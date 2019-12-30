package org.apache.isis.extensions.xdocreport.dom.impl.example.models;

import lombok.Data;

import java.util.List;
import java.util.Map;

import org.apache.isis.extensions.xdocreport.dom.impl.XDocReportModel;

import com.google.common.collect.ImmutableMap;

@Data
public class ProjectDevelopersModel implements XDocReportModel {

    private final Project project;
    private final List<Developer> developers;

    @Override
    public Map<String, Data> getContextData() {
        return ImmutableMap.of(
                "project", Data.object(project),
                "developers", Data.list(developers, Developer.class));
    }

}
