package com.gksyb.demo.database;

import com.easy.query.core.basic.extension.track.EntityState;
import com.easy.query.core.basic.extension.track.EntityTrackProperty;
import com.easy.query.core.basic.extension.track.EntityValueState;
import com.easy.query.core.basic.jdbc.parameter.SQLParameter;
import com.easy.query.core.basic.jdbc.parameter.ToSQLContext;
import com.easy.query.core.expression.sql.expression.EntityPredicateSQLExpression;
import com.easy.query.core.metadata.ColumnMetadata;
import com.easy.query.core.metadata.EntityMetadata;
import com.easy.query.core.metadata.EntityMetadataManager;
import com.easy.query.core.util.EasyBeanUtil;
import com.easy.query.core.util.EasyTrackUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class EasyQueryUtils {

    /**
     * 获取实体的主键值
     */
    public static Object getKey(Object entity, EntityMetadata entityMetadata) {
        Collection<String> keyProperties = entityMetadata.getKeyProperties();
        List<String> values = new ArrayList<>(keyProperties.size());
        for (String keyProperty : keyProperties) {
            ColumnMetadata column = entityMetadata.getColumnNotNull(keyProperty);
            Object value = EasyBeanUtil.getCurrentPropertyValue(entity, column);
            values.add(value.toString());
        }
        return values.size() == 1 ? values.get(0) : String.join(",", values);
    }
}
