package org.springsource.tutorial.domain;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@RooJavaBean
@RooToString
@RooEntity
@JsonIgnoreProperties(ignoreUnknown=true)
public class USState {

    @NotNull
    private String name;

    @NotNull
    @Size(min = 2, max = 2)
    private String abbreviation;

    @NotNull
    private String capital;
    
    public static List<USState> findOrderedUSStateEntries(int firstResult, int maxResults, String orderBy) {
        return entityManager().createQuery("SELECT o FROM USState o ORDER BY o."+orderBy, USState.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
}
