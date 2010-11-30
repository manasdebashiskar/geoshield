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
 * @author Milan Antonovic, Massimiliano Cannata
 */
@Entity
@Table(name = "spr_req", schema = "public", uniqueConstraints = {@UniqueConstraint(columnNames = {"id_req_fk", "id_spr_fk"})})
@NamedQueries({
    @NamedQuery(
        name = "SprReq.findAll",
        query = "SELECT s FROM SprReq s"),
    @NamedQuery(
        name = "SprReq.findByIdSre",
        query = "SELECT s FROM SprReq s WHERE s.idSre = :idSre"),
    @NamedQuery(
        name = "SprReq.findByIdReqIdSpr",
        query = "SELECT s FROM SprReq s " +
                "WHERE s.idReqFk = :idReq " +
                "AND s.idSprFk = :idSpr")
})
public class SprReq implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_sre", nullable = false)
    private Integer idSre;
    @JoinColumn(name = "id_req_fk", referencedColumnName = "id_req", nullable = false)
    @ManyToOne(optional = false)
    private Requests idReqFk;
    @JoinColumn(name = "id_spr_fk", referencedColumnName = "id_spr", nullable = false)
    @ManyToOne(optional = false)
    private ServicesPermissions idSprFk;

    public SprReq() {
    }

    public SprReq(Integer idSre) {
        this.idSre = idSre;
    }

    public Integer getIdSre() {
        return idSre;
    }

    public void setIdSre(Integer idSre) {
        this.idSre = idSre;
    }

    public Requests getIdReqFk() {
        return idReqFk;
    }

    public void setIdReqFk(Requests idReqFk) {
        this.idReqFk = idReqFk;
    }

    public ServicesPermissions getIdSprFk() {
        return idSprFk;
    }

    public void setIdSprFk(ServicesPermissions idSprFk) {
        this.idSprFk = idSprFk;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idSre != null ? idSre.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof SprReq)) {
            return false;
        }
        SprReq other = (SprReq) object;
        if ((this.idSre == null && other.idSre != null) || (this.idSre != null && !this.idSre.equals(other.idSre))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ch.supsi.ist.interceptor.data.SprReq[idSre=" + idSre + "]";
    }

}
