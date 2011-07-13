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
@Table(name = "services_urls", schema = "public", uniqueConstraints = {@UniqueConstraint(columnNames = {"path_sur", "url_sur"}), @UniqueConstraint(columnNames = {"path_sur"})})
@NamedQueries({
    @NamedQuery(
        name = "ServicesUrls.findAll",
        query = "SELECT s FROM ServicesUrls s"),
    @NamedQuery(
        name = "ServicesUrls.findByIdSur",
        query = "SELECT s FROM ServicesUrls s WHERE s.idSur = :idSur"),
    @NamedQuery(
        name = "ServicesUrls.findByPathSur",
        query = "SELECT s FROM ServicesUrls s WHERE s.pathSur = :pathSur"),
    @NamedQuery(
        name = "ServicesUrls.findByPathSurIdSrv",
        query = "SELECT s FROM ServicesUrls s WHERE s.pathSur = :pathSur and s.idSrvFk = :idSrvFk"),
    @NamedQuery(
        name = "ServicesUrls.findByUrlSur",
        query = "SELECT s FROM ServicesUrls s WHERE s.urlSur = :urlSur"),
    @NamedQuery(
        name = "ServicesUrls.findByIdGrp",
        query = "SELECT s FROM ServicesUrls s, ServicesPermissions p " +
                "WHERE s.idSur = p.idSurFk " +
                "AND p.idGrpFk = :idGrpFk ")
})
public class ServicesUrls implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_sur", nullable = false)
    private Integer idSur;

    @Basic(optional = false)
    @Column(name = "path_sur", nullable = false, length = 15)
    private String pathSur;
    
    @Basic(optional = false)
    @Column(name = "url_sur", nullable = false, length = 100)
    private String urlSur;
    
    @Basic(optional = false)
    @Column(name = "usr_sur", nullable = false, length = 100)
    private String usrSur;
    
    @Basic(optional = false)
    @Column(name = "psw_sur", nullable = false, length = 100)
    private String pswSur;

    @JoinColumn(name = "id_srv_fk", referencedColumnName = "id_srv", nullable = false)
    @ManyToOne(optional = false)
    private Services idSrvFk;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "idSurFk")
    private List<Layers> layersCollection;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "idSurFk")
    private List<Offerings> offerings;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "idSurFk")
    private List<ServicesPermissions> servicesPermissionsCollection;

    public ServicesUrls() {
    }

    public ServicesUrls(Integer idSur) {
        this.idSur = idSur;
    }

    public ServicesUrls(Integer idSur, String pathSur, String urlSur) {
        this.idSur = idSur;
        this.pathSur = pathSur;
        this.urlSur = urlSur;
    }

    public Integer getIdSur() {
        return idSur;
    }

    public void setIdSur(Integer idSur) {
        this.idSur = idSur;
    }

    public String getPathSur() {
        return pathSur;
    }

    public void setPathSur(String pathSur) {
        this.pathSur = pathSur;
    }

    public String getUrlSur() {
        return urlSur;
    }

    public void setUrlSur(String urlSur) {
        this.urlSur = urlSur;
    }

    public String getUsrSur() {
        return usrSur;
    }

    public void setUsrSur(String usrSur) {
        this.usrSur = usrSur;
    }

    public String getPswSur() {
        return pswSur;
    }

    public void setPswSur(String pswSur) {
        this.pswSur = pswSur;
    }

    public Services getIdSrvFk() {
        return idSrvFk;
    }

    public void setIdSrvFk(Services idSrvFk) {
        this.idSrvFk = idSrvFk;
    }

    public List<Layers> getLayersCollection() {
        return layersCollection;
    }

    public void setLayersCollection(List<Layers> layersCollection) {
        this.layersCollection = layersCollection;
    }

    public List<Offerings> getOfferings() {
        return offerings;
    }

    public void setOfferings(List<Offerings> offerings) {
        this.offerings = offerings;
    }

    public List<ServicesPermissions> getServicesPermissionsCollection() {
        return servicesPermissionsCollection;
    }
    
    @JSON
    public List<ServicesPermissions> getSprList() {
        return this.servicesPermissionsCollection;
    }

    public void setServicesPermissionsCollection(List<ServicesPermissions> servicesPermissionsCollection) {
        this.servicesPermissionsCollection = servicesPermissionsCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idSur != null ? idSur.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ServicesUrls)) {
            return false;
        }
        ServicesUrls other = (ServicesUrls) object;
        if ((this.idSur == null && other.idSur != null) || (this.idSur != null && !this.idSur.equals(other.idSur))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ch.supsi.ist.geoshield.data.ServicesUrls[idSur=" + idSur + "]";
    }

}
