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
import javax.persistence.UniqueConstraint;

/**
 *
 * @author Milan Antonovic, Massimiliano Cannata
 */
@Entity
@Table(name = "layers", schema = "public", uniqueConstraints = {@UniqueConstraint(columnNames = {"id_sur_fk", "name_lay", "geom_lay"})})
@NamedQueries({
    @NamedQuery(
        name = "Layers.findAll",
        query = "SELECT l FROM Layers l"),
    @NamedQuery(
        name = "Layers.findByIdLay",
        query = "SELECT l FROM Layers l WHERE l.idLay = :idLay"),
    @NamedQuery(
        name = "Layers.findByNameLay",
        query = "SELECT l FROM Layers l WHERE l.nameLay = :nameLay"),
    @NamedQuery(
        name = "Layers.findByGeomLay",
        query = "SELECT l FROM Layers l WHERE l.geomLay = :geomLay"),
    @NamedQuery(
        name = "Layers.findByPathAndName",
        query = "SELECT l FROM Layers l, ServicesUrls s " +
                    "WHERE l.idSurFk.idSur = s.idSur " +
                    "AND s.pathSur = :pathSur " +
                    "AND l.nameLay = :nameLay"
    ),
    @NamedQuery(
        name = "Layers.findByPathAndNameAndService",
        query = "SELECT l FROM Layers l, ServicesUrls s " +
                    "WHERE l.idSurFk.idSur = s.idSur " +
                    "AND s.pathSur = :pathSur " +
                    "AND s.idSrvFk = :idSrvFk " +
                    "AND l.nameLay = :nameLay"
    ),
    @NamedQuery(
        name = "Layers.findByPath",
        query = "SELECT l FROM Layers l, ServicesUrls s " +
                    "WHERE l.idSurFk.idSur = s.idSur " +
                    "AND s.pathSur = :pathSur"
    )
})
public class Layers implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_lay", nullable = false)
    private Integer idLay;

    @Basic(optional = false)
    @Column(name = "name_lay", nullable = false, length = 50)
    private String nameLay;

    @Basic(optional = false)
    @Column(name = "geom_lay", nullable = false, length = 30)
    private String geomLay;

    @Basic(optional = false)
    @Column(name = "ns_lay", nullable = false, length = 50)
    private String nsLay;

    @Basic(optional = false)
    @Column(name = "ns_url_lay", nullable = false, length = 50)
    private String nsUrlLay;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "idLayFk", fetch=FetchType.EAGER)
    private List<LayersPermissions> layersPermissionsCollection;

    @JoinColumn(name = "id_sur_fk", referencedColumnName = "id_sur", nullable = false)
    @ManyToOne(optional = false)
    private ServicesUrls idSurFk;

    public Layers() {
    }

    public Layers(Integer idLay) {
        this.idLay = idLay;
    }

    public Layers(Integer idLay, String nameLay, String geomLay) {
        this.idLay = idLay;
        this.nameLay = nameLay;
        this.geomLay = geomLay;
    }

    public Integer getIdLay() {
        return idLay;
    }

    public void setIdLay(Integer idLay) {
        this.idLay = idLay;
    }

    public String getNameLay() {
        return nameLay;
    }

    public void setNameLay(String nameLay) {
        this.nameLay = nameLay;
    }

    public String getNsLay() {
        return nsLay;
    }

    public void setNsLay(String nsLay) {
        this.nsLay = nsLay;
    }

    public String getNsUrlLay() {
        return nsUrlLay;
    }

    public void setNsUrlLay(String nsUrlLay) {
        this.nsUrlLay = nsUrlLay;
    }

    public String getGeomLay() {
        return geomLay;
    }

    public void setGeomLay(String geomLay) {
        this.geomLay = geomLay;
    }

    public List<LayersPermissions> getLayersPermissionsCollection() {
        return layersPermissionsCollection;
    }

    public void setLayersPermissionsCollection(List<LayersPermissions> layersPermissionsCollection) {
        this.layersPermissionsCollection = layersPermissionsCollection;
    }

    public ServicesUrls getIdSurFk() {
        return idSurFk;
    }

    public void setIdSurFk(ServicesUrls idSurFk) {
        this.idSurFk = idSurFk;
    }

    @JSON
    public List<LayersPermissions> getLayPerList() {
        return layersPermissionsCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idLay != null ? idLay.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Layers)) {
            return false;
        }
        Layers other = (Layers) object;
        if ((this.idLay == null && other.idLay != null) || (this.idLay != null && !this.idLay.equals(other.idLay))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ch.supsi.ist.geoshield.data.Layers[idLay=" + idLay + "]";
    }

}
