use std::sync::Mutex;
use tauri::Manager;
use tauri_plugin_shell::{ShellExt, process::CommandChild};

pub struct BackendState(pub Mutex<Option<CommandChild>>);

#[tauri::command]
async fn start_backend(app: tauri::AppHandle) -> Result<String, String> {
    let state_handle = app.state::<BackendState>();
    let mut state = state_handle.0.lock().unwrap();
    
    if state.is_some() {
        return Ok("Backend already running".to_string());
    }

    let sidecar = app.shell().sidecar("tulipe-backend")
        .map_err(|e| format!("Failed to create sidecar: {}", e))?;
    
    let (_rx, child) = sidecar.spawn()
        .map_err(|e| format!("Failed to spawn sidecar: {}", e))?;
    
    *state = Some(child);
    
    Ok("Backend started successfully".to_string())
}

#[tauri::command]
fn stop_backend(app: tauri::AppHandle) -> Result<String, String> {
    let state_handle = app.state::<BackendState>();
    let mut state = state_handle.0.lock().unwrap();
    if let Some(mut child) = state.take() {
        child.kill().map_err(|e| e.to_string())?;
        Ok("Backend stopped".to_string())
    } else {
        Ok("Backend was not running".to_string())
    }
}

#[tauri::command]
async fn call_test_api() -> Result<String, String> {
    println!("Rust Backend: Attempting to call test API at http://localhost:8080/api/hello");
    let response = reqwest::get("http://localhost:8080/api/hello")
        .await
        .map_err(|e| {
            let err_msg = format!("Failed to call test API: {}", e);
            eprintln!("Rust Backend Error: {}", err_msg);
            err_msg
        })?;
    
    println!("Rust Backend: Received response from test API. Status: {}", response.status());
    
    let text = response.text()
        .await
        .map_err(|e| {
            let err_msg = format!("Failed to read response text: {}", e);
            eprintln!("Rust Backend Error: {}", err_msg);
            err_msg
        })?;
    
    println!("Rust Backend: API Response body: {}", text);
    Ok(text)
}

#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
    tauri::Builder::default()
        .manage(BackendState(Mutex::new(None)))
        .plugin(tauri_plugin_shell::init())
        .plugin(tauri_plugin_opener::init())
        .invoke_handler(tauri::generate_handler![start_backend, stop_backend, call_test_api])
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
