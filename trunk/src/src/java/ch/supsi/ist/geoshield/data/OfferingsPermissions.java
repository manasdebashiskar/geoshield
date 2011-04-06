/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
 * @author milan
 */
@Entity
@Table(name = "offerings_permissions", schema = "public", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"id_off_fk", "id_grp_fk"})})
@NamedQueries({
    @NamedQuery(name = "OfferingsPermissions.findAll",
        query = "SELECT o FROM OfferingsPermissions o"),
    @NamedQuery(name = "OfferingsPermissions.findByIdOpr", 
        query = "SELECT o FROM OfferingsPermissions o WHERE o.idOpr = :idOpr"),
    @NamedQuery(name = "OfferingsPermissions.findByOffAndGrp",
    query = "SELECT o FROM OfferingsPermissions o WHERE o.idOffFk = :idOff and o.idGrpFk = :idGrp")
    })
public class OfferingsPermissions implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_opr", nullable = false)
    private Integer idOpr;

    @JoinColumn(name = "id_grp_fk", referencedColumnName = "id_grp", nullable = false)
    @ManyToOne(optional = false)
    private Groups idGrpFk;
    
    @JoinColumn(name = "id_off_fk", referencedColumnName = "id_off", nullable = false)
    @ManyToOne(optional = false)
    private Offerings idOffFk;

    public OfferingsPermissions() {
    }

    public OfferingsPermissions(Integer idOpr) {
        this.idOpr = idOpr;
    }

    public Integer getIdOpr() {
        return idOpr;
    }

    public void setIdOpr(Integer idOpr) {
        this.idOpr = idOpr;
    }

    public Groups getIdGrpFk() {
        return idGrpFk;
    }

    public void setIdGrpFk(Groups idGrpFk) {
        this.idGrpFk = idGrpFk;
    }

    public Offerings getIdOffFk() {
        return idOffFk;
    }

    public void setIdOffFk(Offerings idOffFk) {
        this.idOffFk = idOffFk;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idOpr != null ? idOpr.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof OfferingsPermissions)) {
            return false;
        }
        OfferingsPermissions other = (OfferingsPermissions) object;
        if ((this.idOpr == null && other.idOpr != null) || (this.idOpr != null && !this.idOpr.equals(other.idOpr))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ch.supsi.ist.geoshield.data.OfferingsPermissions[idOpr=" + idOpr + "]";
    }

}
