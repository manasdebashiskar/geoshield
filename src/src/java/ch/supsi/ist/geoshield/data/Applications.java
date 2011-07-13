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
@Table(name = "applications", schema = "public")
@NamedQueries({@NamedQuery(name = "Applications.findAll", query = "SELECT a FROM Applications a"), @NamedQuery(name = "Applications.findByIdAps", query = "SELECT a FROM Applications a WHERE a.idAps = :idAps"), @NamedQuery(name = "Applications.findByNameAps", query = "SELECT a FROM Applications a WHERE a.nameAps = :nameAps"), @NamedQuery(name = "Applications.findByDescAps", query = "SELECT a FROM Applications a WHERE a.descAps = :descAps"), @NamedQuery(name = "Applications.findByPathAps", query = "SELECT a FROM Applications a WHERE a.pathAps = :pathAps"), @NamedQuery(name = "Applications.findByPortAps", query = "SELECT a FROM Applications a WHERE a.portAps = :portAps"), @NamedQuery(name = "Applications.findByHostAps", query = "SELECT a FROM Applications a WHERE a.hostAps = :hostAps"), @NamedQuery(name = "Applications.findByVpathAps", query = "SELECT a FROM Applications a WHERE a.vpathAps = :vpathAps"), @NamedQuery(name = "Applications.findByIsPublicAps", query = "SELECT a FROM Applications a WHERE a.isPublicAps = :isPublicAps"), @NamedQuery(name = "Applications.findByIsHiddenAps", query = "SELECT a FROM Applications a WHERE a.isHiddenAps = :isHiddenAps"), @NamedQuery(name = "Applications.findByHomeAps", query = "SELECT a FROM Applications a WHERE a.homeAps = :homeAps")})
public class Applications implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id_aps", nullable = false)
    private Integer idAps;

    @Basic(optional = false)
    @Column(name = "name_aps", nullable = false, length = 50)
    private String nameAps;

    @Column(name = "desc_aps", length = 2147483647)
    private String descAps;

    @Column(name = "path_aps", length = 50)
    private String pathAps;

    @Basic(optional = false)
    @Column(name = "port_aps", nullable = false)
    private int portAps;

    @Basic(optional = false)
    @Column(name = "host_aps", nullable = false, length = 100)
    private String hostAps;

    @Column(name = "vpath_aps", length = 20)
    private String vpathAps;

    @Column(name = "is_public_aps")
    private Boolean isPublicAps;

    @Column(name = "is_hidden_aps")
    private Boolean isHiddenAps;

    @Column(name = "home_aps", length = 60)
    private String homeAps;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "idApsFk")
    private List<GrpAps> grpApsCollection;

    public Applications() {
    }

    public Applications(Integer idAps) {
        this.idAps = idAps;
    }

    public Applications(Integer idAps, String nameAps, int portAps, String hostAps) {
        this.idAps = idAps;
        this.nameAps = nameAps;
        this.portAps = portAps;
        this.hostAps = hostAps;
    }

    public Integer getIdAps() {
        return idAps;
    }

    public void setIdAps(Integer idAps) {
        this.idAps = idAps;
    }

    public String getNameAps() {
        return nameAps;
    }

    public void setNameAps(String nameAps) {
        this.nameAps = nameAps;
    }

    public String getDescAps() {
        return descAps;
    }

    public void setDescAps(String descAps) {
        this.descAps = descAps;
    }

    public String getPathAps() {
        return pathAps;
    }

    public void setPathAps(String pathAps) {
        this.pathAps = pathAps;
    }

    public int getPortAps() {
        return portAps;
    }

    public void setPortAps(int portAps) {
        this.portAps = portAps;
    }

    public String getHostAps() {
        return hostAps;
    }

    public void setHostAps(String hostAps) {
        this.hostAps = hostAps;
    }

    public String getVpathAps() {
        return vpathAps;
    }

    public void setVpathAps(String vpathAps) {
        this.vpathAps = vpathAps;
    }

    public Boolean getIsPublicAps() {
        return isPublicAps;
    }

    public void setIsPublicAps(Boolean isPublicAps) {
        this.isPublicAps = isPublicAps;
    }

    public Boolean getIsHiddenAps() {
        return isHiddenAps;
    }

    public void setIsHiddenAps(Boolean isHiddenAps) {
        this.isHiddenAps = isHiddenAps;
    }

    public String getHomeAps() {
        return homeAps;
    }

    public void setHomeAps(String homeAps) {
        this.homeAps = homeAps;
    }

    public List<GrpAps> getGrpApsCollection() {
        return grpApsCollection;
    }

    public void setGrpApsCollection(List<GrpAps> grpApsCollection) {
        this.grpApsCollection = grpApsCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idAps != null ? idAps.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Applications)) {
            return false;
        }
        Applications other = (Applications) object;
        if ((this.idAps == null && other.idAps != null) || (this.idAps != null && !this.idAps.equals(other.idAps))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ch.supsi.ist.geoshield.data.Applications[idAps=" + idAps + "]";
    }

}
