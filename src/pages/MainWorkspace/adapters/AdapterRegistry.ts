import type { DatabaseAdapter } from './DatabaseAdapter';

/**
 * 数据库适配器注册表
 * 负责管理所有数据库类型的适配器
 */
export class AdapterRegistry {
  private adapters = new Map<string, DatabaseAdapter>();
  
  /**
   * 注册数据库适配器
   * @param adapter 数据库适配器实例
   */
  register(adapter: DatabaseAdapter): void {
    this.adapters.set(adapter.dbType, adapter);
  }
  
  /**
   * 获取指定数据库类型的适配器
   * @param dbType 数据库类型
   * @returns 数据库适配器实例或 undefined
   */
  get(dbType: string): DatabaseAdapter | undefined {
    return this.adapters.get(dbType);
  }
  
  /**
   * 获取指定数据库类型的适配器，如果不存在则返回默认适配器
   * @param dbType 数据库类型
   * @returns 数据库适配器实例
   * @throws 如果没有找到适配器且没有默认适配器
   */
  getOrDefault(dbType: string): DatabaseAdapter {
    return this.adapters.get(dbType) ?? this.adapters.get('mysql')!;
  }
  
  /**
   * 获取所有已注册的数据库类型
   * @returns 数据库类型列表
   */
  getSupportedDbTypes(): string[] {
    return Array.from(this.adapters.keys());
  }
}

// 导出全局注册表实例
export const adapterRegistry = new AdapterRegistry();
