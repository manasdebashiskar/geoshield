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
import java.util.ArrayList;
import java.util.Iterator;
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
 * @author Milan Antonovic - milan.antonovic@supsi.ch
 */
@Entity
@Table(name = "users", schema = "public", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name_usr"})})
@NamedQueries({@NamedQuery(name = "Users.findAll", query = "SELECT u FROM Users u"),
@NamedQuery(name = "Users.findByIdUsr", query = "SELECT u FROM Users u WHERE u.idUsr = :idUsr"),
@NamedQuery(name = "Users.findByNameUsr", query = "SELECT u FROM Users u WHERE u.nameUsr = :nameUsr"),
@NamedQuery(name = "Users.findByPswUsr", query = "SELECT u FROM Users u WHERE u.pswUsr = :pswUsr"),
@NamedQuery(name = "Users.findByFirstNameUsr", query = "SELECT u FROM Users u WHERE u.firstNameUsr = :firstNameUsr"),
@NamedQuery(name = "Users.findByLastNameUsr", query = "SELECT u FROM Users u WHERE u.lastNameUsr = :lastNameUsr"),
@NamedQuery(name = "Users.findByEmailUsr", query = "SELECT u FROM Users u WHERE u.emailUsr = :emailUsr"),
@NamedQuery(name = "Users.findByOfficeUsr", query = "SELECT u FROM Users u WHERE u.officeUsr = :officeUsr"),
@NamedQuery(name = "Users.findByTelUsr", query = "SELECT u FROM Users u WHERE u.telUsr = :telUsr"),
@NamedQuery(name = "Users.findByFaxUsr", query = "SELECT u FROM Users u WHERE u.faxUsr = :faxUsr"),
@NamedQuery(name = "Users.findByAddressUsr", query = "SELECT u FROM Users u WHERE u.addressUsr = :addressUsr"),
@NamedQuery(name = "Users.findByIsActiveUsr", query = "SELECT u FROM Users u WHERE u.isActiveUsr = :isActiveUsr")})
public class Users implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_usr", nullable = false)
    private Integer idUsr;

    @Basic(optional = false)
    @Column(name = "name_usr", nullable = false, length = 20)
    private String nameUsr;

    @Column(name = "psw_usr", length = 20)
    private String pswUsr;

    @Column(name = "first_name_usr", length = 50)
    private String firstNameUsr;

    @Column(name = "last_name_usr", length = 50)
    private String lastNameUsr;

    @Column(name = "email_usr", length = 100)
    private String emailUsr;

    @Column(name = "office_usr", length = 100)
    private String officeUsr;

    @Column(name = "tel_usr", length = 20)
    private String telUsr;

    @Column(name = "fax_usr", length = 20)
    private String faxUsr;

    @Column(name = "address_usr", length = 2147483647)
    private String addressUsr;

    @Column(name = "is_active_usr")
    private Boolean isActiveUsr;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "idUsrFk", fetch=FetchType.EAGER)
    private List<GroupsUsers> groupsUsers;

    public Users() {
    }

    public Users(Integer idUsr) {
        this.idUsr = idUsr;
    }

    public Users(Integer idUsr, String nameUsr, String pswUsr) {
        this.idUsr = idUsr;
        this.nameUsr = nameUsr;
        this.pswUsr = pswUsr;
    }

    @JSON
    public Integer getIdUsr() {
        return idUsr;
    }

    @JSON(include = false)
    public void setIdUsr(Integer idUsr) {
        this.idUsr = idUsr;
    }

    @JSON
    public String getNameUsr() {
        return nameUsr;
    }

    @JSON(include = false)
    public void setNameUsr(String nameUsr) {
        this.nameUsr = nameUsr;
    }

    @JSON
    public String getPswUsr() {
        return pswUsr;
    }

    @JSON(include = false)
    public void setPswUsr(String pswUsr) {
        this.pswUsr = pswUsr;
    }

    @JSON(include = false)
    public List<Groups> getGroups() {
        //System.out.println("groupsUsersCollection: " + groupsUsersCollection.toString());
        if (groupsUsers == null || groupsUsers.size() == 0) {
            return null;
        }
        List<Groups> ret = new ArrayList<Groups>();
        for (Iterator<GroupsUsers> it = groupsUsers.iterator(); it.hasNext();) {
            GroupsUsers gu = it.next();
            ret.add(gu.getIdGrpFk());
        }
        return ret;
    }

    @JSON
    public String getFirstNameUsr() {
        return firstNameUsr;
    }

    public void setFirstNameUsr(String firstNameUsr) {
        this.firstNameUsr = firstNameUsr;
    }

    @JSON
    public String getLastNameUsr() {
        return lastNameUsr;
    }

    public void setLastNameUsr(String lastNameUsr) {
        this.lastNameUsr = lastNameUsr;
    }

    @JSON
    public String getEmailUsr() {
        return emailUsr;
    }

    public void setEmailUsr(String emailUsr) {
        this.emailUsr = emailUsr;
    }

    public String getOfficeUsr() {
        return officeUsr;
    }

    public void setOfficeUsr(String officeUsr) {
        this.officeUsr = officeUsr;
    }

    public String getTelUsr() {
        return telUsr;
    }

    public void setTelUsr(String telUsr) {
        this.telUsr = telUsr;
    }

    public String getFaxUsr() {
        return faxUsr;
    }

    public void setFaxUsr(String faxUsr) {
        this.faxUsr = faxUsr;
    }

    public String getAddressUsr() {
        return addressUsr;
    }

    public void setAddressUsr(String addressUsr) {
        this.addressUsr = addressUsr;
    }

    public Boolean getIsActiveUsr() {
        if(isActiveUsr==null){
            return Boolean.FALSE;
        }
        return isActiveUsr;
    }

    public void setIsActiveUsr(Boolean isActiveUsr) {
        this.isActiveUsr = isActiveUsr;
    }

    @JSON(include = true)
    public List<GroupsUsers> getGroupsUsers() {
        return groupsUsers;
    }

    @JSON(include = false)
    public void setGroupsUsers(List<GroupsUsers> groupsUsersCollection) {
        this.groupsUsers = groupsUsersCollection;
    }

    @JSON(include = false)
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idUsr != null ? idUsr.hashCode() : 0);
        return hash;
    }

    @JSON(include = false)
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Users)) {
            return false;
        }
        Users other = (Users) object;
        if ((this.idUsr == null && other.idUsr != null) || (this.idUsr != null && !this.idUsr.equals(other.idUsr))) {
            return false;
        }
        return true;
    }

    @JSON(include = false)
    @Override
    public String toString() {
        return "ch.supsi.ist.geoshield.data.Users[idUsr=" + idUsr + ", name=" + nameUsr + ",lastname=" + lastNameUsr + "]";
    }
}
