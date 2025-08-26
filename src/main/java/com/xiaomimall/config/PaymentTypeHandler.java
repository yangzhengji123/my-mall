package com.xiaomimall.config;

import com.xiaomimall.entity.PaymentType;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(PaymentType.class)
@MappedJdbcTypes(JdbcType.INTEGER)
public class PaymentTypeHandler extends BaseTypeHandler<PaymentType> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, PaymentType parameter, JdbcType jdbcType) throws SQLException {
        // 将枚举的 code 属性写入数据库
        ps.setInt(i, parameter.getCode());
    }

    @Override
    public PaymentType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        // 从数据库读取整数值，并转换为枚举
        int code = rs.getInt(columnName);
        return rs.wasNull() ? null : PaymentType.fromCode(code);
    }

    @Override
    public PaymentType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int code = rs.getInt(columnIndex);
        return rs.wasNull() ? null : PaymentType.fromCode(code);
    }

    @Override
    public PaymentType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int code = cs.getInt(columnIndex);
        return cs.wasNull() ? null : PaymentType.fromCode(code);
    }
}