import type { Component } from 'vue';
import type { DatabaseAdapter } from './DatabaseAdapter';
import MySqlWorkspace from '../components/MySqlWorkspace.vue';

/**
 * MySQL 数据库适配器
 * 提供 MySQL 专用的 UI 组件
 */
export class MySqlAdapter implements DatabaseAdapter {
  readonly id = 'mysql-adapter';
  readonly dbType = 'mysql';

  /**
   * 获取对象导航组件
   * MySQL 使用标准的关系型数据库导航（Tables/Functions/Views 等）
   */
  getNavigator(): Component | null {
    // MySQL 使用标准的关系型数据库导航
    return null; // 导航组件已集成在 MySqlWorkspace 中
  }

  /**
   * 获取主内容区组件
   * 返回 MySQL 专用的工作区组件
   */
  getMainPanel(): Component {
    return MySqlWorkspace;
  }

  /**
   * 获取详情面板组件
   * MySQL 使用标准的表结构详情面板
   */
  getDetailsPanel(): Component | null {
    // 详情面板已集成在 MySqlWorkspace 中
    return null;
  }
}
