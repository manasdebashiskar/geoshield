/**
 * Copyright (c) 2010 Istituto Scienze della Terra - SUPSI
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Istituto Scienze della Terra - SUPSI nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE ISTITUTO SCIENZE DELLA TERRA - SUPSI BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package ch.supsi.ist.geoshield.data;

import flexjson.JSON;
import java.io.Serializable;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 *
 * @author Milan Antonovic, Massimiliano Cannata
 */
@Entity
@Table(name = "groups", schema = "public", uniqueConstraints = {@UniqueConstraint(columnNames = {"name_grp"})})
@NamedQueries(
    {
    @NamedQuery(
        name = "Groups.findAll",
        query = "SELECT g FROM Groups g"),
    @NamedQuery(
        name = "Groups.findByIdGrp",
        query = "SELECT g FROM Groups g WHERE g.idGrp = :idGrp"),
    @NamedQuery(
        name = "Groups.findByNameGrp",
        query = "SELECT g FROM Groups g WHERE g.nameGrp = :nameGrp")
})
public class Groups implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_grp", nullable = false)
    private Integer idGrp;

    @Column(name = "name_grp", length = 100)
    private String nameGrp;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "idGrpFk", fetch=FetchType.EAGER)
    private List<LayersPermissions> layersPermissionsCollection;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "idGrpFk", fetch=FetchType.EAGER)
    private List<OfferingsPermissions> offeringsPermissions;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "idGrpFk", fetch=FetchType.EAGER)
    private List<GroupsUsers> groupsUsersCollection;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "idGrpFk", fetch=FetchType.EAGER)
    private List<ServicesPermissions> servicesPermissionsCollection;

    public Groups() {
    }

    public Groups(Integer idGrp) {
        this.idGrp = idGrp;
    }

    public Groups(Integer idGrp, String nameGrp) {
        this.idGrp = idGrp;
        this.nameGrp = nameGrp;
    }

    @JSON
    public Integer getIdGrp() {
        return idGrp;
    }

    public void setIdGrp(Integer idGrp) {
        this.idGrp = idGrp;
    }

    @JSON
    public String getNameGrp() {
        return nameGrp;
    }

    public void setNameGrp(String nameGrp) {
        this.nameGrp = nameGrp;
    }

    public List<LayersPermissions> getLayersPermissionsCollection() {
        return layersPermissionsCollection;
    }

    public void setLayersPermissionsCollection(List<LayersPermissions> layersPermissionsCollection) {
        this.layersPermissionsCollection = layersPermissionsCollection;
    }

    public List<OfferingsPermissions> getOfferingsPermissions() {
        return offeringsPermissions;
    }

    public void setOfferingsPermissions(List<OfferingsPermissions> offeringsPermissions) {
        this.offeringsPermissions = offeringsPermissions;
    }

    public List<GroupsUsers> getGroupsUsersCollection() {
        return groupsUsersCollection;
    }

    public void setGroupsUsersCollection(List<GroupsUsers> groupsUsersCollection) {
        this.groupsUsersCollection = groupsUsersCollection;
    }

    public List<ServicesPermissions> getServicesPermissionsCollection() {
        return servicesPermissionsCollection;
    }

    public void setServicesPermissionsCollection(List<ServicesPermissions> servicesPermissionsCollection) {
        this.servicesPermissionsCollection = servicesPermissionsCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idGrp != null ? idGrp.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Groups)) {
            return false;
        }
        Groups other = (Groups) object;
        if ((this.idGrp == null && other.idGrp != null) || (this.idGrp != null && !this.idGrp.equals(other.idGrp))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ch.supsi.ist.interceptor.data.Groups[idGrp=" + idGrp + "]";
    }

}
