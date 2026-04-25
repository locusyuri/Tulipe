import type { Component } from 'vue';
import type { DatabaseAdapter } from './DatabaseAdapter';
import MySqlWorkspace from '../components/MySqlWorkspace.vue';

/**
 * PostgreSQL 数据库适配器
 * 提供 PostgreSQL 专用的 UI 组件，支持 Schemas
 */
export class PostgreSqlAdapter implements DatabaseAdapter {
  readonly id = 'postgresql-adapter';
  readonly dbType = 'postgresql';

  /**
   * 获取对象导航组件
   * PostgreSQL 使用关系型数据库导航，包含 Schemas
   */
  getNavigator(): Component | null {
    // PostgreSQL 使用标准的关系型数据库导航，包含 Schemas
    return null; // 导航组件已集成在 MySqlWorkspace 中
  }

  /**
   * 获取主内容区组件
   * 返回 PostgreSQL 专用的工作区组件
   */
  getMainPanel(): Component {
    return MySqlWorkspace; // 复用 MySQL 工作区组件，后续可根据需要创建专用组件
  }

  /**
   * 获取详情面板组件
   * PostgreSQL 使用标准的表结构详情面板
   */
  getDetailsPanel(): Component | null {
    // 详情面板已集成在 MySqlWorkspace 中
    return null;
  }
}
