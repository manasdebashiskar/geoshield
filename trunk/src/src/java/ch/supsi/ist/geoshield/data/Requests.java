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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 *
 * @author Milan Antonovic, Massimiliano Cannata
 */
@Entity
@Table(name = "requests", schema = "public")
@NamedQueries({
    @NamedQuery(
        name = "Requests.findAll",
        query = "SELECT r FROM Requests r"),
    @NamedQuery(
        name = "Requests.findByIdReq",
        query = "SELECT r FROM Requests r WHERE r.idReq = :idReq"),
    @NamedQuery(
        name = "Requests.findByNameReq",
        query = "SELECT r FROM Requests r WHERE UPPER(r.nameReq) = UPPER(:nameReq)"),
    @NamedQuery(
        name = "Requests.findByNameReqidSrvFk",
        query = "SELECT r FROM Requests r " +
                "WHERE UPPER(r.nameReq) = UPPER(:nameReq) " +
                "AND r.idSrvFk = :idSrvFk ")
})
public class Requests implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_req", nullable = false)
    private Integer idReq;

    @Basic(optional = false)
    @Column(name = "name_req", nullable = false, length = 20)
    private String nameReq;

    @JoinColumn(name = "id_srv_fk", referencedColumnName = "id_srv", nullable = false)
    @ManyToOne(optional = false)
    private Services idSrvFk;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "idReqFk", fetch=FetchType.EAGER)
    private List<SprReq> sprReqCollection;

    public Requests() {
    }

    public Requests(Integer idReq) {
        this.idReq = idReq;
    }

    public Requests(Integer idReq, String nameReq) {
        this.idReq = idReq;
        this.nameReq = nameReq;
    }

    public Integer getIdReq() {
        return idReq;
    }

    public void setIdReq(Integer idReq) {
        this.idReq = idReq;
    }

    public String getNameReq() {
        return nameReq;
    }

    public void setNameReq(String nameReq) {
        this.nameReq = nameReq;
    }

    public Services getIdSrvFk() {
        return idSrvFk;
    }

    public void setIdSrvFk(Services idSrvFk) {
        this.idSrvFk = idSrvFk;
    }

    @JSON
    public List<SprReq> getSreList() {
        return sprReqCollection;
    }

    public void setSreList(List<SprReq> sprReqCollection) {
        this.sprReqCollection = sprReqCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idReq != null ? idReq.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Requests)) {
            return false;
        }
        Requests other = (Requests) object;
        if ((this.idReq == null && other.idReq != null) || (this.idReq != null && !this.idReq.equals(other.idReq))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ch.supsi.ist.geoshield.data.Requests[idReq=" + idReq + "]";
    }

}
