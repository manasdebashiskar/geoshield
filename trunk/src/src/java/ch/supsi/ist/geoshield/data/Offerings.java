/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.supsi.ist.geoshield.data;

import flexjson.JSON;
import java.io.Serializable;
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
 * @author milan
 */
@Entity
@Table(name = "offerings", catalog = "geoshield", schema = "public", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"id_sur_fk", "name_off"})})
@NamedQueries({
    @NamedQuery(name = "Offerings.findAll", query = "SELECT o FROM Offerings o"),
    @NamedQuery(name = "Offerings.findByIdOff", query = "SELECT o FROM Offerings o WHERE o.idOff = :idOff"),
    @NamedQuery(name = "Offerings.findByNameOff", query = "SELECT o FROM Offerings o WHERE o.nameOff = :nameOff")})
public class Offerings implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_off", nullable = false)
    private Integer idOff;

    @Basic(optional = false)
    @Column(name = "name_off", nullable = false, length = 50)
    private String nameOff;

    @Basic(optional = true)
    @Column(name = "desc_off", nullable = true, length = 100)
    private String descOff;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "idOffFk")
    private List<OfferingsPermissions> offeringsPermissionsList;

    @JoinColumn(name = "id_sur_fk", referencedColumnName = "id_sur", nullable = false)
    @ManyToOne(optional = false)
    private ServicesUrls idSurFk;

    public Offerings() {
    }

    public Offerings(Integer idOff) {
        this.idOff = idOff;
    }

    public Offerings(Integer idOff, String nameOff) {
        this.idOff = idOff;
        this.nameOff = nameOff;
    }

    public Integer getIdOff() {
        return idOff;
    }

    public void setIdOff(Integer idOff) {
        this.idOff = idOff;
    }

    public String getNameOff() {
        return nameOff;
    }

    public void setNameOff(String nameOff) {
        this.nameOff = nameOff;
    }

    public String getDescOff() {
        return descOff;
    }

    public void setDescOff(String descOff) {
        this.descOff = descOff;
    }

    @JSON
    public List<OfferingsPermissions> getOffPerList() {
        return offeringsPermissionsList;
    }

    public List<OfferingsPermissions> getOfferingsPermissionsList() {
        return offeringsPermissionsList;
    }

    public void setOfferingsPermissionsList(List<OfferingsPermissions> offeringsPermissionsList) {
        this.offeringsPermissionsList = offeringsPermissionsList;
    }

    public ServicesUrls getIdSurFk() {
        return idSurFk;
    }

    public void setIdSurFk(ServicesUrls idSurFk) {
        this.idSurFk = idSurFk;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idOff != null ? idOff.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Offerings)) {
            return false;
        }
        Offerings other = (Offerings) object;
        if ((this.idOff == null && other.idOff != null) || (this.idOff != null && !this.idOff.equals(other.idOff))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ch.supsi.ist.geoshield.data.Offerings[idOff=" + idOff + "]";
    }

}
