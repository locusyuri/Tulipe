use crate::backend::state::get_backend_port;
use reqwest::header::{HeaderMap, HeaderName, HeaderValue, CONTENT_TYPE};
use reqwest::{Client, Method, Url};
use serde::{Deserialize, Serialize};
use std::collections::HashMap;
use std::time::Duration;

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct BackendProxyRequest {
    method: String,
    path: String,
    query: Option<HashMap<String, String>>,
    headers: Option<HashMap<String, String>>,
    body: Option<String>,
    timeout_ms: Option<u64>,
}

#[derive(Debug, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct BackendProxyResponse {
    status: u16,
    ok: bool,
    headers: HashMap<String, String>,
    body: String,
    content_type: Option<String>,
}

#[tauri::command]
pub async fn proxy_backend_request(
    app: tauri::AppHandle,
    request: BackendProxyRequest,
) -> Result<BackendProxyResponse, String> {
    // 将前端传入的方法字符串转换为 reqwest 可识别的 HTTP 方法。
    let method = Method::from_bytes(request.method.as_bytes())
        .map_err(|e| format!("Invalid HTTP method: {}", e))?;

    // 只允许绝对路径，避免拼接 URL 时出现歧义。
    if !request.path.starts_with("/") {
        return Err("Path must start with '/'".to_string());
    }

    // 将 Rust 层作为受控网关，仅放行 /api/ 前缀。
    if !request.path.starts_with("/api/") {
        return Err("Path must start with '/api/'".to_string());
    }

    // 从状态里读取当前后端端口，确保代理请求总是打到实际运行端口。
    let port = get_backend_port(&app)?;

    // 统一拼接动态端口地址与请求路径。
    let mut url = Url::parse(&format!("http://127.0.0.1:{}{}", port, request.path))
        .map_err(|e| format!("Invalid request URL: {}", e))?;

    if let Some(query) = request.query {
        // 透传 query 参数，避免前端自行拼接 URL。
        let mut pairs = url.query_pairs_mut();
        for (key, value) in query {
            pairs.append_pair(&key, &value);
        }
    }

    // 限制超时时间上下界，防止异常值导致请求行为不可控。
    let timeout_ms = request.timeout_ms.unwrap_or(15_000).clamp(100, 120_000);
    let client = Client::new();
    let mut builder = client
        .request(method.clone(), url)
        .timeout(Duration::from_millis(timeout_ms));

    if let Some(headers) = request.headers {
        // 对头名称和值做严格校验，非法时直接返回错误给前端。
        let mut header_map = HeaderMap::new();
        for (key, value) in headers {
            let name = HeaderName::from_bytes(key.as_bytes())
                .map_err(|e| format!("Invalid header name '{}': {}", key, e))?;
            let val = HeaderValue::from_str(&value)
                .map_err(|e| format!("Invalid header value for '{}': {}", key, e))?;
            header_map.insert(name, val);
        }
        builder = builder.headers(header_map);
    }

    if let Some(body) = request.body {
        // GET/HEAD 语义上通常不带请求体，这里显式忽略。
        if method != Method::GET && method != Method::HEAD {
            builder = builder.body(body);
        }
    }

    let response = builder
        .send()
        .await
        .map_err(|e| format!("Failed to call backend API: {}", e))?;

    let status = response.status();
    let headers = response.headers().clone();
    // 提前提取 Content-Type，便于前端按类型决定解析策略。
    let content_type = headers
        .get(CONTENT_TYPE)
        .and_then(|v| v.to_str().ok())
        .map(|v| v.to_string());

    let mut header_map = HashMap::new();
    // 只透传可转为 UTF-8 字符串的响应头。
    for (key, value) in &headers {
        if let Ok(v) = value.to_str() {
            header_map.insert(key.to_string(), v.to_string());
        }
    }

    let body = response
        .text()
        .await
        .map_err(|e| format!("Failed to read backend response text: {}", e))?;

    Ok(BackendProxyResponse {
        status: status.as_u16(),
        ok: status.is_success(),
        headers: header_map,
        body,
        content_type,
    })
}
