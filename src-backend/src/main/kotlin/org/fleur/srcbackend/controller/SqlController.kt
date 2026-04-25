package org.fleur.srcbackend.controller

import org.fleur.srcbackend.pojo.dto.ExecuteSqlRequest
import org.fleur.srcbackend.pojo.vo.SqlExecutionResult
import org.fleur.srcbackend.pojo.vo.SqlMutationResult
import org.fleur.srcbackend.pojo.vo.SqlQueryResult
import org.fleur.srcbackend.result.TulipeResult
import org.fleur.srcbackend.service.DataSourceService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/sql")
class SqlController(
    private val dataSourceService: DataSourceService,
) {

    @PostMapping("/query")
    // 查询类接口只返回 rows，避免和写入类结果混在一起。
    fun query(@RequestBody request: ExecuteSqlRequest): TulipeResult<SqlQueryResult> {
        val result = dataSourceService.executeQuery(request)
        return TulipeResult.success(result)
    }

    @PostMapping("/update")
    // 更新类接口用于 INSERT / UPDATE 这类写入语句，只返回影响行数。
    fun update(@RequestBody request: ExecuteSqlRequest): TulipeResult<SqlMutationResult> {
        val result = dataSourceService.executeUpdate(request)
        return TulipeResult.success(result)
    }

    @PostMapping("/delete")
    // 删除类接口只处理 DELETE，语义更明确，便于前端区分。
    fun delete(@RequestBody request: ExecuteSqlRequest): TulipeResult<SqlMutationResult> {
        val result = dataSourceService.executeDelete(request)
        return TulipeResult.success(result)
    }

    @PostMapping("/ddl")
    // DDL 接口处理 CREATE / ALTER / DROP / TRUNCATE，便于与 DML 接口隔离。
    fun ddl(@RequestBody request: ExecuteSqlRequest): TulipeResult<SqlMutationResult> {
        val result = dataSourceService.executeDdl(request)
        return TulipeResult.success(result)
    }

    @PostMapping("/execute")
    // 旧统一执行入口保留作兼容，前端迁移期间可继续调用。
    @Deprecated("Use /query, /update, /delete, /ddl")
    @Suppress("DEPRECATION")
    fun executeSql(@RequestBody request: ExecuteSqlRequest): TulipeResult<SqlExecutionResult> {
        val result = dataSourceService.executeSql(request)
        return TulipeResult.success(result)
    }
}

