// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package org.hyperic.hq.inventory.domain;

import java.lang.Integer;
import java.lang.Long;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import javax.persistence.Version;

import org.hibernate.annotations.GenericGenerator;
import org.hyperic.hq.inventory.domain.Config;
import org.springframework.transaction.annotation.Transactional;

privileged aspect Config_Roo_Entity {
    
    declare @type: Config: @Entity;
    
    @PersistenceContext
    transient EntityManager Config.entityManager;
    
    @Id
    @GenericGenerator(name="id_gen",strategy="org.hyperic.hq.id.IdentifierGenerator")
    @GeneratedValue(generator="id_gen")
    @Column(name = "id")
    private Long Config.id;
    
    @Version
    @Column(name = "version")
    private Integer Config.version;
    
    public Long Config.getId() {
        return this.id;
    }
    
    public void Config.setId(Long id) {
        this.id = id;
    }
    
    public Integer Config.getVersion() {
        return this.version;
    }
    
    public void Config.setVersion(Integer version) {
        this.version = version;
    }
    
    @Transactional
    public void Config.persist() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.persist(this);
    }
    
    @Transactional
    public void Config.remove() {
        if (this.entityManager == null) this.entityManager = entityManager();
        if (this.entityManager.contains(this)) {
            this.entityManager.remove(this);
        } else {
            Config attached = this.entityManager.find(this.getClass(), this.id);
            this.entityManager.remove(attached);
        }
    }
    
    @Transactional
    public void Config.flush() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.flush();
    }
    
    @Transactional
    public Config Config.merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        Config merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }
    
    public static final EntityManager Config.entityManager() {
        EntityManager em = new Config().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }
    
    public static long Config.countConfigs() {
        return entityManager().createQuery("select count(o) from Config o", Long.class).getSingleResult();
    }
    
    public static List<Config> Config.findAllConfigs() {
        return entityManager().createQuery("select o from Config o", Config.class).getResultList();
    }
    
    public static Config Config.findConfig(Long id) {
        if (id == null) return null;
        return entityManager().find(Config.class, id);
    }
    
    public static List<Config> Config.findConfigEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("select o from Config o", Config.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
}
