import { invoke } from "@tauri-apps/api/core";

export type HttpMethod =
	| "GET"
	| "POST"
	| "PUT"
	| "PATCH"
	| "DELETE"
	| "HEAD"
	| "OPTIONS";

/**
 * 后端代理请求对象，包含 HTTP 方法、路径、查询参数、请求头、请求体和超时设置
 */
export interface BackendProxyRequest {
	method: HttpMethod;
	path: string;
	query?: Record<string, string>;
	headers?: Record<string, string>;
	body?: string;
	timeoutMs?: number;
}

/**
 * 后端代理响应对象，包含 HTTP 状态码、响应体、响应头和内容类型
 */
export interface BackendProxyResponse {
	status: number;
	ok: boolean;
	headers: Record<string, string>;
	body: string;
	contentType?: string;
}

/**
 * 后端请求选项，包含查询参数、请求头和超时设置
 */
export interface BackendRequestOptions {
	query?: Record<string, string>;
	headers?: Record<string, string>;
	timeoutMs?: number;
}


// ----------------------------------------------------------------------------------------
// 一下两个方法是最底层的核心方法，直接与 Tauri IPC 交互，其他方法都是基于它们构建的更高层次的封装。
/**
 * 向后端发送 HTTP 请求并获取响应
 * @param request - 后端代理请求对象，包含 HTTP 方法、路径、查询参数、请求头、请求体和超时设置
 * @returns  一个 Promise，解析为后端代理响应对象，包含 HTTP 状态码、响应体、响应头和内容类型
 * @throws 当 Tauri IPC 调用失败时抛出错误，包含错误信息
 */
export async function requestBackend(
	request: BackendProxyRequest,
): Promise<BackendProxyResponse> {
	// 统一通过 Tauri IPC 进入 Rust 网关，再由 Rust 转发到 SpringBoot。
	return invoke<BackendProxyResponse>("proxy_backend_request", { request });
}

/**
 * 向后端发送请求并解析 JSON 响应
 * @param request - 后端代理请求对象
 * @returns 解析后的 JSON 响应数据
 * @throws 当 HTTP 响应状态码非 200 时抛出错误，包含状态码和响应体
 * @throws 当 JSON 解析失败时抛出错误，包含解析错误信息
 */
export async function requestBackendJson<T>(
	request: BackendProxyRequest,
): Promise<T> {
	const response = await requestBackend(request);
	if (!response.ok) {
		// 保持错误信息可追溯：HTTP 状态码 + 后端响应体。
		throw new Error(`HTTP ${response.status}: ${response.body}`);
	}

	try {
		return JSON.parse(response.body) as T;
	} catch (error) {
		throw new Error(`Failed to parse JSON response: ${String(error)}`);
	}
}

// ----------------------------------------------------------------------------------------


// ----------------------------------------------------------------------------------------
// 以下是基于上述核心方法的更高层次封装，提供了更简洁的接口来发送不同 HTTP 方法的请求，并可选地解析 JSON 响应。
// 包括了 GET、POST、PUT、PATCH、DELETE 等方法的封装，以及对应的 JSON 版本，方便在不同场景下使用。
export function requestBackendGet(
	path: string,
	options: BackendRequestOptions = {},
): Promise<BackendProxyResponse> {
	return requestBackend({ method: "GET", path, ...options });
}


export function requestBackendDelete(
	path: string,
	options: BackendRequestOptions = {},
): Promise<BackendProxyResponse> {
	return requestBackend({ method: "DELETE", path, ...options });
}


function requestBackendWithBody(
	method: "POST" | "PUT" | "PATCH",
	path: string,
	body: string,
	options: BackendRequestOptions = {},
): Promise<BackendProxyResponse> {
	return requestBackend({ method, path, body, ...options });
}

export function requestBackendPost(
	path: string,
	body: string,
	options: BackendRequestOptions = {},
): Promise<BackendProxyResponse> {
	return requestBackendWithBody("POST", path, body, options);
}

export function requestBackendPut(
	path: string,
	body: string,
	options: BackendRequestOptions = {},
): Promise<BackendProxyResponse> {
	return requestBackendWithBody("PUT", path, body, options);
}

export function requestBackendPatch(
	path: string,
	body: string,
	options: BackendRequestOptions = {},
): Promise<BackendProxyResponse> {
	return requestBackendWithBody("PATCH", path, body, options);
}

export function requestBackendGetJson<T>(
	path: string,
	options: BackendRequestOptions = {},
): Promise<T> {
	return requestBackendJson<T>({ method: "GET", path, ...options });
}

export function requestBackendDeleteJson<T>(
	path: string,
	options: BackendRequestOptions = {},
): Promise<T> {
	return requestBackendJson<T>({ method: "DELETE", path, ...options });
}

function requestBackendJsonWithBody<TReq, TRes>(
	method: "POST" | "PUT" | "PATCH",
	path: string,
	body: TReq,
	options: BackendRequestOptions = {},
): Promise<TRes> {
	return requestBackendJson<TRes>({
		method,
		path,
		body: JSON.stringify(body),
		headers: {
			"Content-Type": "application/json",
			...(options.headers ?? {}),
		},
		query: options.query,
		timeoutMs: options.timeoutMs,
	});
}

export function requestBackendPostJson<TReq, TRes>(
	path: string,
	body: TReq,
	options: BackendRequestOptions = {},
): Promise<TRes> {
	return requestBackendJsonWithBody<TReq, TRes>("POST", path, body, options);
}

export function requestBackendPutJson<TReq, TRes>(
	path: string,
	body: TReq,
	options: BackendRequestOptions = {},
): Promise<TRes> {
	return requestBackendJsonWithBody<TReq, TRes>("PUT", path, body, options);
}

export function requestBackendPatchJson<TReq, TRes>(
	path: string,
	body: TReq,
	options: BackendRequestOptions = {},
): Promise<TRes> {
	return requestBackendJsonWithBody<TReq, TRes>("PATCH", path, body, options);
}

// ----------------------------------------------------------------------------------------