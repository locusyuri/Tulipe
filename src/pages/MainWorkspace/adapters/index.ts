import { adapterRegistry } from './AdapterRegistry';
import { MySqlAdapter } from './MySqlAdapter';
import { PostgreSqlAdapter } from './PostgreSqlAdapter';
import { RedisAdapter } from './RedisAdapter';

// 注册所有数据库适配器
adapterRegistry.register(new MySqlAdapter());
adapterRegistry.register(new PostgreSqlAdapter());
adapterRegistry.register(new RedisAdapter());

// 导出适配器注册表
export { adapterRegistry };
export type { DatabaseAdapter } from './DatabaseAdapter';
