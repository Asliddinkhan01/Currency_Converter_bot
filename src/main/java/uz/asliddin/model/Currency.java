
package uz.asliddin.model;

import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class Currency {

    @SerializedName("Ccy")
    private String mCcy;
    @SerializedName("CcyNm_EN")
    private String mCcyNmEN;
    @SerializedName("CcyNm_RU")
    private String mCcyNmRU;
    @SerializedName("CcyNm_UZ")
    private String mCcyNmUZ;
    @SerializedName("CcyNm_UZC")
    private String mCcyNmUZC;
    @SerializedName("Code")
    private String mCode;
    @SerializedName("Date")
    private String mDate;
    @SerializedName("Diff")
    private String mDiff;
    @SerializedName("id")
    private Long mId;
    @SerializedName("Nominal")
    private String mNominal;
    @SerializedName("Rate")
    private String mRate;

    public String getCcy() {
        return mCcy;
    }

    public void setCcy(String ccy) {
        mCcy = ccy;
    }

    public String getCcyNmEN() {
        return mCcyNmEN;
    }

    public void setCcyNmEN(String ccyNmEN) {
        mCcyNmEN = ccyNmEN;
    }

    public String getCcyNmRU() {
        return mCcyNmRU;
    }

    public void setCcyNmRU(String ccyNmRU) {
        mCcyNmRU = ccyNmRU;
    }

    public String getCcyNmUZ() {
        return mCcyNmUZ;
    }

    public void setCcyNmUZ(String ccyNmUZ) {
        mCcyNmUZ = ccyNmUZ;
    }

    public String getCcyNmUZC() {
        return mCcyNmUZC;
    }

    public void setCcyNmUZC(String ccyNmUZC) {
        mCcyNmUZC = ccyNmUZC;
    }

    public String getCode() {
        return mCode;
    }

    public void setCode(String code) {
        mCode = code;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public String getDiff() {
        return mDiff;
    }

    public void setDiff(String diff) {
        mDiff = diff;
    }

    public Long getId() {
        return mId;
    }

    public void setId(Long id) {
        mId = id;
    }

    public String getNominal() {
        return mNominal;
    }

    public void setNominal(String nominal) {
        mNominal = nominal;
    }

    public String getRate() {
        return mRate;
    }

    public void setRate(String rate) {
        mRate = rate;
    }

}
