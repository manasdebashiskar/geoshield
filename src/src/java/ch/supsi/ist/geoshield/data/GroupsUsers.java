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
import java.util.Date;
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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

/**
 *
 * @author Milan Antonovic - milan.antonovic@supsi.ch
 */
@Entity
@Table(name = "groups_users", schema = "public", uniqueConstraints = {@UniqueConstraint(columnNames = {"id_grp_fk", "id_usr_fk"})})
@NamedQueries({
    @NamedQuery(name = "GroupsUsers.findAll",
    query = "SELECT g FROM GroupsUsers g"),
    @NamedQuery(name = "GroupsUsers.findByIdGus",
    query = "SELECT g FROM GroupsUsers g WHERE g.idGus = :idGus"),
    @NamedQuery(name = "GroupsUsers.findByExpirationGus",
    query = "SELECT g FROM GroupsUsers g WHERE g.expirationGus = :expirationGus"),
    @NamedQuery(name = "GroupsUsers.findByInvoiceGus",
    query = "SELECT g FROM GroupsUsers g WHERE g.invoiceGus = :invoiceGus"),
    @NamedQuery(name = "GroupsUsers.findByIdUsr",
    query = "SELECT g FROM GroupsUsers g WHERE g.idUsrFk = :idUsr"),
    @NamedQuery(name = "GroupsUsers.findByGrgUsr",
    query = "SELECT g FROM GroupsUsers g WHERE g.idGrpFk = :idGrp AND g.idUsrFk = :idUsr")
})
public class GroupsUsers implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_gus", nullable = false)
    private Integer idGus;

    @Column(name = "expiration_gus")
    @Temporal(TemporalType.TIMESTAMP)
    private Date expirationGus;

    @Column(name = "invoice_gus", length = 100)
    private String invoiceGus;

    @JoinColumn(name = "id_grp_fk", referencedColumnName = "id_grp", nullable = false)
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Groups idGrpFk;

    @JoinColumn(name = "id_usr_fk", referencedColumnName = "id_usr", nullable = false)
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Users idUsrFk;

    public GroupsUsers() {
    }

    public GroupsUsers(Integer idGus) {
        this.idGus = idGus;
    }

    public Integer getIdGus() {
        return idGus;
    }

    public void setIdGus(Integer idGus) {
        this.idGus = idGus;
    }

    public Date getExpirationGus() {
        return expirationGus;
    }

    public void setExpirationGus(Date expirationGus) {
        this.expirationGus = expirationGus;
    }

    public String getInvoiceGus() {
        return invoiceGus;
    }

    public void setInvoiceGus(String invoiceGus) {
        this.invoiceGus = invoiceGus;
    }

    public Groups getIdGrpFk() {
        return idGrpFk;
    }

    public void setIdGrpFk(Groups idGrpFk) {
        this.idGrpFk = idGrpFk;
    }

    public Users getIdUsrFk() {
        return idUsrFk;
    }

    public void setIdUsrFk(Users idUsrFk) {
        this.idUsrFk = idUsrFk;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idGus != null ? idGus.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof GroupsUsers)) {
            return false;
        }
        GroupsUsers other = (GroupsUsers) object;
        if ((this.idGus == null && other.idGus != null) || (this.idGus != null && !this.idGus.equals(other.idGus))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ch.supsi.ist.interceptor.data.GroupsUsers[idGus=" + idGus + "]";
    }
}
