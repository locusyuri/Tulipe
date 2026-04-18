mod backend;

use backend::{proxy_backend_request, start_backend, stop_backend, BackendState};
use std::sync::Mutex;

#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
    tauri::Builder::default()
        .manage(BackendState(Mutex::new(None)))
        .plugin(tauri_plugin_shell::init())
        .plugin(tauri_plugin_opener::init())
        // 注册后端代理命令，使前端能够通过 invoke 调用 Rust 层的代理函数。
        .invoke_handler(tauri::generate_handler![start_backend, stop_backend, proxy_backend_request])
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
