package ru.greatbit.quack.dal.impl;

import ru.greatbit.quack.beans.ProjectGroup;
import ru.greatbit.quack.dal.ProjectGroupRepositoryCustom;

public class ProjectGroupRepositoryCustomImpl extends CommonRepositoryImpl<ProjectGroup>
        implements ProjectGroupRepositoryCustom {

    @Override
    public Class<ProjectGroup> getEntityClass() {
        return ProjectGroup.class;
    }

    @Override
    protected String getCollectionName(String projectId) {
        return "project-groups";
    }
}
