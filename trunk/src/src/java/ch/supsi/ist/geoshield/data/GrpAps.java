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
 * @author Milan Antonovic - milan.antonovic@supsi.ch
 */
@Entity
@Table(name = "grp_aps", schema = "public", uniqueConstraints = {@UniqueConstraint(columnNames = {"id_grp_fk", "id_aps_fk"})})
@NamedQueries({
    @NamedQuery(name = "GrpAps.findAll",
    query = "SELECT g FROM GrpAps g"),
    @NamedQuery(name = "GrpAps.findByIdGra",
    query = "SELECT g FROM GrpAps g WHERE g.idGra = :idGra"),
    @NamedQuery(name = "GrpAps.findByGrpAndApp",
    query = "SELECT l FROM GrpAps l WHERE l.idApsFk = :idAps and l.idGrpFk = :idGrp")})
public class GrpAps implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_gra", nullable = false)
    private Integer idGra;
    @JoinColumn(name = "id_aps_fk", referencedColumnName = "id_aps", nullable = false)
    @ManyToOne(optional = false)
    private Applications idApsFk;
    @JoinColumn(name = "id_grp_fk", referencedColumnName = "id_grp", nullable = false)
    @ManyToOne(optional = false)
    private Groups idGrpFk;

    public GrpAps() {
    }

    public GrpAps(Integer idGra) {
        this.idGra = idGra;
    }

    public Integer getIdGra() {
        return idGra;
    }

    public void setIdGra(Integer idGra) {
        this.idGra = idGra;
    }

    public Applications getIdApsFk() {
        return idApsFk;
    }

    public void setIdApsFk(Applications idApsFk) {
        this.idApsFk = idApsFk;
    }

    public Groups getIdGrpFk() {
        return idGrpFk;
    }

    public void setIdGrpFk(Groups idGrpFk) {
        this.idGrpFk = idGrpFk;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idGra != null ? idGra.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof GrpAps)) {
            return false;
        }
        GrpAps other = (GrpAps) object;
        if ((this.idGra == null && other.idGra != null) || (this.idGra != null && !this.idGra.equals(other.idGra))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ch.supsi.ist.geoshield.data.GrpAps[idGra=" + idGra + "]";
    }
}
