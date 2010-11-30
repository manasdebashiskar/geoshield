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

import java.io.Serializable;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
@Table(name = "services_permissions", schema = "public", uniqueConstraints = {@UniqueConstraint(columnNames = {"id_sur_fk", "id_grp_fk"})})
@NamedQueries({
    @NamedQuery(
        name = "ServicesPermissions.findAll",
        query = "SELECT s FROM ServicesPermissions s"),
    @NamedQuery(
        name = "ServicesPermissions.findByIdSpr",
        query = "SELECT s FROM ServicesPermissions s WHERE s.idSpr = :idSpr"),
    @NamedQuery(
        name = "ServicesPermissions.findBySurGrp",
        query = "SELECT s FROM ServicesPermissions s " +
                "WHERE s.idGrpFk = :idGrpFk " +
                "AND s.idSurFk = :idSurFk ")
})
public class ServicesPermissions implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_spr", nullable = false)
    private Integer idSpr;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "idSprFk")
    private List<SprReq> sprReqCollection;
    @JoinColumn(name = "id_grp_fk", referencedColumnName = "id_grp", nullable = false)
    @ManyToOne(optional = false)
    private Groups idGrpFk;
    @JoinColumn(name = "id_sur_fk", referencedColumnName = "id_sur", nullable = false)
    @ManyToOne(optional = false)
    private ServicesUrls idSurFk;

    public ServicesPermissions() {
    }

    public ServicesPermissions(Integer idSpr) {
        this.idSpr = idSpr;
    }

    public Integer getIdSpr() {
        return idSpr;
    }

    public void setIdSpr(Integer idSpr) {
        this.idSpr = idSpr;
    }

    public List<SprReq> getSprReqCollection() {
        return sprReqCollection;
    }

    public void setSprReqCollection(List<SprReq> sprReqCollection) {
        this.sprReqCollection = sprReqCollection;
    }

    public Groups getIdGrpFk() {
        return idGrpFk;
    }

    public void setIdGrpFk(Groups idGrpFk) {
        this.idGrpFk = idGrpFk;
    }

    public ServicesUrls getIdSurFk() {
        return idSurFk;
    }

    public void setIdSurFk(ServicesUrls idSurFk) {
        this.idSurFk = idSurFk;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idSpr != null ? idSpr.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ServicesPermissions)) {
            return false;
        }
        ServicesPermissions other = (ServicesPermissions) object;
        if ((this.idSpr == null && other.idSpr != null) || (this.idSpr != null && !this.idSpr.equals(other.idSpr))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ch.supsi.ist.interceptor.data.ServicesPermissions[idSpr=" + idSpr + "]";
    }

}
