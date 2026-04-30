package org.fleur.srcbackend.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.fleur.srcbackend.pojo.dto.StructuredDdlRequest
import org.fleur.srcbackend.pojo.dto.StructuredDeleteRequest
import org.fleur.srcbackend.pojo.dto.ExecuteSqlReq
import org.fleur.srcbackend.pojo.dto.StructuredInsertRequest
import org.fleur.srcbackend.pojo.dto.StructuredQueryRequest
import org.fleur.srcbackend.pojo.dto.StructuredUpdateRequest
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
@Tag(name = "SQL", description = "结构化 SQL 与兼容执行接口")
class SqlController(
    private val dataSourceService: DataSourceService,
) {

    @PostMapping("/query")
    @Operation(summary = "结构化查询", description = "按结构化条件查询数据并返回 rows")
    // 查询接口使用结构化入参，避免前端直接拼接原始 SQL。
    fun query(@RequestBody request: StructuredQueryRequest): TulipeResult<SqlQueryResult> {
        val result = dataSourceService.executeQuery(request)
        return TulipeResult.success(result)
    }

    @PostMapping("/insert")
    @Operation(summary = "结构化插入", description = "按 table + values 执行参数化 INSERT")
    // 插入接口使用结构化 values，由后端拼接参数化 INSERT。
    fun insert(@RequestBody request: StructuredInsertRequest): TulipeResult<SqlMutationResult> {
        val result = dataSourceService.executeInsert(request)
        return TulipeResult.success(result)
    }

    @PostMapping("/update")
    @Operation(summary = "结构化更新", description = "按 setValues + filters 执行参数化 UPDATE")
    // 更新接口使用结构化 setValues + filters，降低误更新风险。
    fun update(@RequestBody request: StructuredUpdateRequest): TulipeResult<SqlMutationResult> {
        val result = dataSourceService.executeUpdate(request)
        return TulipeResult.success(result)
    }

    @PostMapping("/delete")
    @Operation(summary = "结构化删除", description = "按 filters 执行参数化 DELETE，默认避免全表删除")
    // 删除接口使用结构化 filters，默认要求条件避免误删全表。
    fun delete(@RequestBody request: StructuredDeleteRequest): TulipeResult<SqlMutationResult> {
        val result = dataSourceService.executeDelete(request)
        return TulipeResult.success(result)
    }

    @PostMapping("/ddl")
    @Operation(summary = "结构化 DDL", description = "执行 CREATE / DROP / TRUNCATE 结构化 DDL")
    // DDL 接口使用结构化 action/table（当前首版支持 CREATE / DROP / TRUNCATE）。
    fun ddl(@RequestBody request: StructuredDdlRequest): TulipeResult<SqlMutationResult> {
        val result = dataSourceService.executeDdl(request)
        return TulipeResult.success(result)
    }

    @PostMapping("/execute")
    @Operation(summary = "兼容原始 SQL 执行", description = "兼容旧版前端的原始 SQL 执行入口", deprecated = true)
    // 旧统一执行入口保留作兼容，前端迁移期间可继续调用。
    @Deprecated("Use /query, /insert, /update, /delete, /ddl")
    @Suppress("DEPRECATION")
    fun executeSql(@RequestBody request: ExecuteSqlReq): TulipeResult<SqlExecutionResult> {
        val result = dataSourceService.executeSql(request)
        return TulipeResult.success(result)
    }
}

