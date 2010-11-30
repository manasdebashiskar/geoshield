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
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 *
 * @author Milan Antonovic, Massimiliano Cannata
 */
@Entity
@Table(name = "layers_permissions", schema = "public", uniqueConstraints = {@UniqueConstraint(columnNames = {"id_lay_fk", "id_grp_fk"})})
@NamedQueries({
    @NamedQuery(name = "LayersPermissions.findAll",
    query = "SELECT l FROM LayersPermissions l"),
    @NamedQuery(name = "LayersPermissions.findByIdLpr",
    query = "SELECT l FROM LayersPermissions l WHERE l.idLpr = :idLpr"),
    @NamedQuery(name = "LayersPermissions.findByLayAndGrp",
    query = "SELECT l FROM LayersPermissions l WHERE l.idLayFk = :idLay and l.idGrpFk = :idGrp")
})
public class LayersPermissions implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_lpr", nullable = false)
    private Integer idLpr;

    @Column(name = "filter_lpr", length = 2147483647)
    private String filterLpr;

    @JoinColumn(name = "id_grp_fk", referencedColumnName = "id_grp", nullable = false)
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Groups idGrpFk;

    @JoinColumn(name = "id_lay_fk", referencedColumnName = "id_lay", nullable = false)
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Layers idLayFk;

    public LayersPermissions() {
        filterLpr = "INCLUDE";
    }

    public LayersPermissions(Integer idLpr) {
        this.idLpr = idLpr;
    }

    public Integer getIdLpr() {
        return idLpr;
    }

    public void setIdLpr(Integer idLpr) {
        this.idLpr = idLpr;
    }

    public String getFilterLpr() {
        if (filterLpr == null || filterLpr.equals("")) {
            return "INCLUDE";
        } else {
            return filterLpr;
        }
    }

    public void setFilterLpr(String filterLpr) {
        if (filterLpr == null || filterLpr.equals("")) {
            this.filterLpr = "INCLUDE";
        } else {
            this.filterLpr = filterLpr;
        }
    }

    public Groups getIdGrpFk() {
        return idGrpFk;
    }

    public void setIdGrpFk(Groups idGrpFk) {
        this.idGrpFk = idGrpFk;
    }

    public Layers getIdLayFk() {
        return idLayFk;
    }

    public void setIdLayFk(Layers idLayFk) {
        this.idLayFk = idLayFk;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idLpr != null ? idLpr.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof LayersPermissions)) {
            return false;
        }
        LayersPermissions other = (LayersPermissions) object;
        if ((this.idLpr == null && other.idLpr != null) || (this.idLpr != null && !this.idLpr.equals(other.idLpr))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ch.supsi.ist.interceptor.data.LayersPermissions[idLpr=" + idLpr + "]";
    }
}
