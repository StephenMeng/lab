package com.stephen.lab.model.paper;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "kiva_simple")
public class KivaSimple {
    @Id
    @Column(name = "id")
    private Long id;
    @Column(name = "original_description")
    private String originalDescription;
    @Column(name = "standard_description")
    private String standardDescription;
    @Column(name = "tags")
    private String tags;
    @Column(name = "gen_tags")
    private String genTags;

    public KivaSimple() {

    }

    public KivaSimple(Kiva kiva) {
        setId(kiva.getId());
        setOriginalDescription(kiva.getOriginal_description());
        setTags(kiva.getTags());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOriginalDescription() {
        return originalDescription;
    }

    public void setOriginalDescription(String originalDescription) {
        this.originalDescription = originalDescription;
    }

    public String getStandardDescription() {
        return standardDescription;
    }

    public void setStandardDescription(String standardDescription) {
        this.standardDescription = standardDescription;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getGenTags() {
        return genTags;
    }

    public void setGenTags(String genTags) {
        this.genTags = genTags;
    }
}
