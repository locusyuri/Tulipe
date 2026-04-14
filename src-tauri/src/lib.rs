use std::process::{Command, Child};
use std::sync::Mutex;
use tauri::Manager;

pub struct BackendState(pub Mutex<Option<Child>>);

#[tauri::command]
fn start_backend(app: tauri::AppHandle) -> Result<String, String> {
    // 根据当前平台和架构解析路径
    let backend_path = app.path().resolve(
        "binaries/x86_64-pc-windows-msvc/src-backend.exe",
        tauri::path::BaseDirectory::Resource
    ).map_err(|e| e.to_string())?;
    
    if !backend_path.exists() {
        return Err(format!("Backend executable not found at {:?}", backend_path));
    }

    let mut state = app.state::<BackendState>().0.lock().unwrap();
    if state.is_some() {
        return Ok("Backend already running".to_string());
    }

    let child = Command::new(backend_path)
        .spawn()
        .map_err(|e| format!("Failed to start backend: {}", e))?;
    
    *state = Some(child);
    
    Ok("Backend started successfully".to_string())
}

#[tauri::command]
fn stop_backend(app: tauri::AppHandle) -> Result<String, String> {
    let mut state = app.state::<BackendState>().0.lock().unwrap();
    if let Some(mut child) = state.take() {
        child.kill().map_err(|e| e.to_string())?;
        Ok("Backend stopped".to_string())
    } else {
        Ok("Backend was not running".to_string())
    }
}

#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
    tauri::Builder::default()
        .manage(BackendState(Mutex::new(None)))
        .plugin(tauri_plugin_opener::init())
        .invoke_handler(tauri::generate_handler![start_backend, stop_backend])
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
