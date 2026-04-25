import type { Component } from 'vue';
import type { DatabaseAdapter } from './DatabaseAdapter';

/**
 * Redis 数据库适配器
 * 提供 Redis 专用的 UI 组件
 */
export class RedisAdapter implements DatabaseAdapter {
  readonly id = 'redis-adapter';
  readonly dbType = 'redis';

  /**
   * 获取对象导航组件
   * Redis 不需要对象导航，直接显示 Key-Value 列表
   */
  getNavigator(): Component | null {
    return null; // Redis 不需要对象导航
  }

  /**
   * 获取主内容区组件
   * 返回 Redis 专用的 Key-Value 列表组件
   */
  getMainPanel(): Component {
    // 后续创建 Redis 专用的 Key-Value 列表组件
    // 暂时返回一个占位组件
    return {
      template: `
        <div class="p-4">
          <h2 class="text-xl font-bold mb-4">Redis Key-Value List</h2>
          <div class="text-muted-foreground">
            Redis 工作区组件开发中...
          </div>
        </div>
      `
    };
  }

  /**
   * 获取详情面板组件
   * Redis 使用 Key 属性详情面板
   */
  getDetailsPanel(): Component | null {
    // 后续创建 Redis 专用的 Key 属性详情组件
    return null;
  }
}
