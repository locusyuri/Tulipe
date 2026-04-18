use crate::backend::state::{BackendProcess, BackendState};
use reqwest::Client;
use std::net::TcpListener;
use std::thread;
use std::time::{Duration, Instant};
use tauri::Manager;
use tauri_plugin_shell::ShellExt;

#[tauri::command]
pub async fn start_backend(app: tauri::AppHandle) -> Result<String, String> {
    {
        let state_handle = app.state::<BackendState>();
        let state = state_handle.0.lock().unwrap();

        if let Some(process) = state.as_ref() {
            return Ok(format!("Backend already running on port {}", process.port));
        }
    }

    // 先向系统申请一个当前空闲的本地端口，避免固定端口冲突。
    let port = allocate_free_port()?;

    // 通过 Tauri sidecar 启动 SpringBoot Native 可执行文件。
    let sidecar = app
        .shell()
        .sidecar("tulipe-backend")
        .map_err(|e| format!("Failed to create sidecar: {}", e))?;

    let (_rx, child) = sidecar
        .args([
            format!("--server.port={}", port),
            "--server.address=127.0.0.1".to_string(),
        ])
        .spawn()
        .map_err(|e| format!("Failed to spawn sidecar: {}", e))?;

    // 等待后端端口真正可访问，避免前端紧接着请求时出现连接拒绝。
    wait_backend_ready(port).await?;

    let state_handle = app.state::<BackendState>();
    let mut state = state_handle.0.lock().unwrap();
    if let Some(process) = state.as_ref() {
        // 极端并发下若其他调用已经完成启动，这里返回已运行信息。
        return Ok(format!("Backend already running on port {}", process.port));
    }

    *state = Some(BackendProcess { child, port });

    Ok(format!("Backend started successfully on port {}", port))
}

#[tauri::command]
pub fn stop_backend(app: tauri::AppHandle) -> Result<String, String> {
    let state_handle = app.state::<BackendState>();
    let mut state = state_handle.0.lock().unwrap();
    if let Some(process) = state.take() {
        process.child.kill().map_err(|e| e.to_string())?;
        Ok("Backend stopped".to_string())
    } else {
        Ok("Backend was not running".to_string())
    }
}

// 向操作系统申请空闲端口：绑定 127.0.0.1:0 后读取实际分配的端口号。
fn allocate_free_port() -> Result<u16, String> {
    let listener = TcpListener::bind("127.0.0.1:0")
        .map_err(|e| format!("Failed to bind an available port: {}", e))?;
    listener
        .local_addr()
        .map(|addr| addr.port())
        .map_err(|e| format!("Failed to read allocated port: {}", e))
}

// 启动后轮询根路径，任意 HTTP 响应都视为后端已就绪。
async fn wait_backend_ready(port: u16) -> Result<(), String> {
    let client = Client::new();
    let deadline = Instant::now() + Duration::from_secs(10);
    let health_url = format!("http://127.0.0.1:{}/", port);

    while Instant::now() < deadline {
        if client.get(&health_url).send().await.is_ok() {
            return Ok(());
        }
        thread::sleep(Duration::from_millis(150));
    }

    Err(format!(
        "Backend did not become ready in time on port {}",
        port
    ))
}
