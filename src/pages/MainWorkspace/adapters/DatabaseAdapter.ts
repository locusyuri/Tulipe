import type { Component } from 'vue';

/**
 * 数据库适配器接口
 * 每个数据库类型实现此接口，提供自己的 UI 组件
 */
export interface DatabaseAdapter {
  /** 适配器唯一标识 */
  readonly id: string;
  
  /** 数据库类型 */
  readonly dbType: string;
  
  /**
   * 获取对象导航组件（可选，返回 null 则不显示左侧栏）
   * 例如：MySQL 返回 Tables/Functions 导航面板
   *      Redis 返回 null（不需要对象导航）
   */
  getNavigator(): Component | null;
  
  /**
   * 获取主内容区组件（必选）
   * 这是数据展示的核心区域
   * 例如：MySQL 返回表格组件，Redis 返回 Key-Value 组件，Neo4j 返回图可视化
   */
  getMainPanel(): Component;
  
  /**
   * 获取详情面板组件（可选，返回 null 则不显示右侧栏）
   * 例如：MySQL 返回表结构详情，Redis 返回 Key 属性详情
   */
  getDetailsPanel(): Component | null;
}
