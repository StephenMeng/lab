package com.stephen.lab.model.paper;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "kiva")
public class Kiva implements Serializable {
    @Id
    @Column(name = "id")
    private Long id;
    @Basic
    @Column(name = "user_name")
    private String name;
    @Basic
    @Column(name = "original_language")
    private String originalLanguage;
    @Basic
    @Column(name = "original_description")
    private String original_description;
    @Basic
    @Column(name = "translated_description")
    private String translatedDescription;
    @Basic
    @Column(name = "funded_amount")
    private Integer fundedAmount;
    @Basic
    @Column(name = "loan_amount")
    private Integer loanAmount;
    @Basic
    @Column(name = "status")
    private String status;
    @Basic
    @Column(name = "image_id")
    private Long imageId;
    @Basic
    @Column(name = "video_id")
    private Long videoId;
    @Basic
    @Column(name = "activity")
    private String activity;
    @Basic
    @Column(name = "sector")
    private String sector;
    @Basic
    @Column(name = "to_use")
    private String use;
    @Basic
    @Column(name = "country_code")
    private String countryCode;
    @Basic
    @Column(name = "country_name")
    private String countryName;
    @Basic
    @Column(name = "town")
    private String town;
    @Basic
    @Column(name = "currency_policy")
    private String currencyPolicy;
    @Basic
    @Column(name = "currency_exchange_coverage_rate")
    private Double currencyExchangeCoverageRate;
    @Basic
    @Column(name = "currency")
    private String currency;
    @Basic
    @Column(name = "partner_id")
    private Integer partnerId;
    @Basic
    @Column(name = "posted_time")
    private Date postedTime;
    @Basic
    @Column(name = "planned_expiration_time")
    private Date plannedExpirationTime;
    @Basic
    @Column(name = "disbursed_time")
    private Date disbursedTime;
    @Basic
    @Column(name = "funded_time")
    private Date fundedTime;
    @Basic
    @Column(name = "term_in_months")
    private Integer termInMonths;
    @Basic
    @Column(name = "lender_count")
    private Integer lenderCount;
    @Basic
    @Column(name = "journal_entries_count")
    private Integer journalEntriesCount;
    @Basic
    @Column(name = "bulk_journal_entries_count")
    private Integer bulkJournalEntriesCount;
    @Basic
    @Column(name = "tags")
    private String tags;
    @Basic
    @Column(name = "repayment_interval")
    private String repaymentInterval;
    @Basic
    @Column(name = "distribution_model")
    private String distributionModel;
    @Basic
    @Column(name = "borrowers")
    private String borrowers;

    public Kiva() {
    }

    public Kiva(KivaSimple kivaSimple) {
        setId(kivaSimple.getId());
        setOriginalLanguage(kivaSimple.getOriginalDescription());
        setTags(kivaSimple.getTags());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOriginalLanguage() {
        return originalLanguage;
    }

    public void setOriginalLanguage(String originalLanguage) {
        this.originalLanguage = originalLanguage;
    }

    public String getOriginal_description() {
        return original_description;
    }

    public void setOriginal_description(String original_description) {
        this.original_description = original_description;
    }

    public String getTranslatedDescription() {
        return translatedDescription;
    }

    public void setTranslatedDescription(String translatedDescription) {
        this.translatedDescription = translatedDescription;
    }

    public Integer getFundedAmount() {
        return fundedAmount;
    }

    public void setFundedAmount(Integer fundedAmount) {
        this.fundedAmount = fundedAmount;
    }

    public Integer getLoanAmount() {
        return loanAmount;
    }

    public void setLoanAmount(Integer loanAmount) {
        this.loanAmount = loanAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getImageId() {
        return imageId;
    }

    public void setImageId(Long imageId) {
        this.imageId = imageId;
    }

    public Long getVideoId() {
        return videoId;
    }

    public void setVideoId(Long videoId) {
        this.videoId = videoId;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getUse() {
        return use;
    }

    public void setUse(String use) {
        this.use = use;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public String getCurrencyPolicy() {
        return currencyPolicy;
    }

    public void setCurrencyPolicy(String currencyPolicy) {
        this.currencyPolicy = currencyPolicy;
    }

    public Double getCurrencyExchangeCoverageRate() {
        return currencyExchangeCoverageRate;
    }

    public void setCurrencyExchangeCoverageRate(Double currencyExchangeCoverageRate) {
        this.currencyExchangeCoverageRate = currencyExchangeCoverageRate;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Integer getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(Integer partnerId) {
        this.partnerId = partnerId;
    }

    public Date getPostedTime() {
        return postedTime;
    }

    public void setPostedTime(Date postedTime) {
        this.postedTime = postedTime;
    }

    public Date getPlannedExpirationTime() {
        return plannedExpirationTime;
    }

    public void setPlannedExpirationTime(Date plannedExpirationTime) {
        this.plannedExpirationTime = plannedExpirationTime;
    }

    public Date getDisbursedTime() {
        return disbursedTime;
    }

    public void setDisbursedTime(Date disbursedTime) {
        this.disbursedTime = disbursedTime;
    }

    public Date getFundedTime() {
        return fundedTime;
    }

    public void setFundedTime(Date fundedTime) {
        this.fundedTime = fundedTime;
    }

    public Integer getTermInMonths() {
        return termInMonths;
    }

    public void setTermInMonths(Integer termInMonths) {
        this.termInMonths = termInMonths;
    }

    public Integer getLenderCount() {
        return lenderCount;
    }

    public void setLenderCount(Integer lenderCount) {
        this.lenderCount = lenderCount;
    }

    public Integer getJournalEntriesCount() {
        return journalEntriesCount;
    }

    public void setJournalEntriesCount(Integer journalEntriesCount) {
        this.journalEntriesCount = journalEntriesCount;
    }

    public Integer getBulkJournalEntriesCount() {
        return bulkJournalEntriesCount;
    }

    public void setBulkJournalEntriesCount(Integer bulkJournalEntriesCount) {
        this.bulkJournalEntriesCount = bulkJournalEntriesCount;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getRepaymentInterval() {
        return repaymentInterval;
    }

    public void setRepaymentInterval(String repaymentInterval) {
        this.repaymentInterval = repaymentInterval;
    }

    public String getDistributionModel() {
        return distributionModel;
    }

    public void setDistributionModel(String distributionModel) {
        this.distributionModel = distributionModel;
    }

    public String getBorrowers() {
        return borrowers;
    }

    public void setBorrowers(String borrowers) {
        this.borrowers = borrowers;
    }

    @Override
    public String toString() {
        return "Kiva{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", originalLanguage='" + originalLanguage + '\'' +
                ", original_description='" + original_description + '\'' +
                ", translatedDescription='" + translatedDescription + '\'' +
                ", fundedAmount=" + fundedAmount +
                ", loanAmount=" + loanAmount +
                ", status=" + status +
                ", imageId=" + imageId +
                ", videoId=" + videoId +
                ", activity='" + activity + '\'' +
                ", sector='" + sector + '\'' +
                ", use='" + use + '\'' +
                ", countryCode='" + countryCode + '\'' +
                ", countryName='" + countryName + '\'' +
                ", town='" + town + '\'' +
                ", currencyPolicy='" + currencyPolicy + '\'' +
                ", currencyExchangeCoverageRate=" + currencyExchangeCoverageRate +
                ", currency='" + currency + '\'' +
                ", partnerId=" + partnerId +
                ", postedTime=" + postedTime +
                ", plannedExpirationTime=" + plannedExpirationTime +
                ", disbursedTime=" + disbursedTime +
                ", fundedTime=" + fundedTime +
                ", termInMonths=" + termInMonths +
                ", lenderCount=" + lenderCount +
                ", journalEntriesCount=" + journalEntriesCount +
                ", bulkJournalEntriesCount=" + bulkJournalEntriesCount +
                ", tags='" + tags + '\'' +
                ", repaymentInterval='" + repaymentInterval + '\'' +
                ", distributionModel='" + distributionModel + '\'' +
                ", borrowers='" + borrowers + '\'' +
                '}';
    }
}
