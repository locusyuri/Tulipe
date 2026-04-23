package org.fleur.srcbackend.repository

import org.fleur.srcbackend.pojo.entity.ConnectionProfile
import org.springframework.stereotype.Repository
import javax.sql.DataSource
import java.sql.Statement

@Repository
class ConnectionProfileRepository(
    private val dataSource: DataSource,
) {

    fun findById(id: Long): ConnectionProfile? {
        val sql = """
            SELECT
                id,
                profile_name,
                db_type,
                host,
                port,
                database_name,
                username,
                password_ciphertext,
                extra_json
            FROM connection_profile
            WHERE id = ?
        """.trimIndent()

        return dataSource.connection.use { con ->
            con.prepareStatement(sql).use { ps ->
                ps.setLong(1, id)
                ps.executeQuery().use { rs ->
                    if (!rs.next()) return@use null

                    ConnectionProfile(
                        id = rs.getLong("id"),
                        profileName = rs.getString("profile_name"),
                        dbType = rs.getString("db_type"),
                        host = rs.getString("host"),
                        port = rs.getInt("port"),
                        databaseName = rs.getString("database_name"),
                        username = rs.getString("username"),
                        passwordCiphertext = rs.getString("password_ciphertext"),
                        extraJson = rs.getString("extra_json"),
                    )
                }
            }
        }
    }

    fun save(profile: ConnectionProfile): Long {
        val sql = """
            INSERT INTO connection_profile (
                profile_name,
                db_type,
                host,
                port,
                database_name,
                username,
                password_ciphertext,
                extra_json
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

        return dataSource.connection.use { con ->
            con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { ps ->
                ps.setString(1, profile.profileName)
                ps.setString(2, profile.dbType)
                ps.setString(3, profile.host)
                ps.setInt(4, profile.port)
                ps.setString(5, profile.databaseName)
                ps.setString(6, profile.username)
                ps.setString(7, profile.passwordCiphertext)
                ps.setString(8, profile.extraJson)
                ps.executeUpdate()

                ps.generatedKeys.use { rs ->
                    if (rs.next()) {
                        rs.getLong(1)
                    } else {
                        throw IllegalStateException("保存连接配置失败，未返回主键")
                    }
                }
            }
        }
    }
}



