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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 *
 * @author Milan Antonovic, Massimiliano Cannata
 */
@Entity
@Table(name = "services", schema = "public")
@NamedQueries({
    @NamedQuery(
        name = "Services.findAll",
        query = "SELECT s FROM Services s"),
    @NamedQuery(
        name = "Services.findByIdSrv",
        query = "SELECT s FROM Services s WHERE s.idSrv = :idSrv"),
    @NamedQuery(
        name = "Services.findByNameSrv",
        query = "SELECT s FROM Services s WHERE UPPER(s.nameSrv) = UPPER(:nameSrv)")
})
public class Services implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_srv", nullable = false)
    private Integer idSrv;
    @Column(name = "name_srv", length = 20)
    private String nameSrv;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "idSrvFk")
    private List<ServicesUrls> servicesUrlsCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "idSrvFk")
    private List<Requests> requestsCollection;

    public Services() {
    }

    public Services(Integer idSrv) {
        this.idSrv = idSrv;
    }

    public Services(Integer idSrv, String nameReq) {
        this.idSrv = idSrv;
        this.nameSrv = nameReq;
    }

    public Integer getIdSrv() {
        return idSrv;
    }

    public void setIdSrv(Integer idSrv) {
        this.idSrv = idSrv;
    }

    public String getNameSrv() {
        return nameSrv;
    }

    public void setNameSrv(String nameSrv) {
        this.nameSrv = nameSrv;
    }

    public List<Requests> getRequestsCollection() {
        return requestsCollection;
    }

    public void setRequestsCollection(List<Requests> requestsCollection) {
        this.requestsCollection = requestsCollection;
    }

    public List<ServicesUrls> getServicesUrlsCollection() {
        return servicesUrlsCollection;
    }

    public void setServicesUrlsCollection(List<ServicesUrls> servicesUrlsCollection) {
        this.servicesUrlsCollection = servicesUrlsCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idSrv != null ? idSrv.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Services)) {
            return false;
        }
        Services other = (Services) object;
        if ((this.idSrv == null && other.idSrv != null) || (this.idSrv != null && !this.idSrv.equals(other.idSrv))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ch.supsi.ist.geoshield.data.Services[idSrv=" + idSrv + "]";
    }

}
