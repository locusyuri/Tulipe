use std::sync::Mutex;
use tauri::AppHandle;
use tauri::Manager;
use tauri_plugin_shell::process::CommandChild;

pub struct BackendProcess {
    pub child: CommandChild,
    pub port: u16,
}

pub struct BackendState(pub Mutex<Option<BackendProcess>>);

// 统一从状态中读取当前后端端口，避免代理层直接操作底层结构。
pub fn get_backend_port(app: &AppHandle) -> Result<u16, String> {
    let state_handle = app.state::<BackendState>();
    let state = state_handle.0.lock().unwrap();
    state
        .as_ref()
        .map(|process| process.port)
        .ok_or_else(|| "Backend is not running".to_string())
}
