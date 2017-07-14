package com.muntako.radio.model;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Channel implements Parcelable, Serializable{

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("site_add")
    @Expose
    private String siteAdd;
    @SerializedName("url_stream_stereo")
    @Expose
    private String urlStreamStereo;
    @SerializedName("url_stream_mono")
    @Expose
    private String urlStreamMono;
    @SerializedName("stat_mono")
    @Expose
    private Integer statMono;
    @SerializedName("stat_stereo")
    @Expose
    private Integer statStereo;
    @SerializedName("path_logo")
    @Expose
    private String pathlogo;
    @SerializedName("kota")
    @Expose
    private String kota;
    @SerializedName("frekuensi")
    @Expose
    private String frekuensi;
    @SerializedName("band")
    @Expose
    private String band;
    @SerializedName("kategori")
    @Expose
    private String kategori;
    @SerializedName("kode_provinsi")
    @Expose
    private String kodepropinsi;
    @SerializedName("nama_provinsi")
    @Expose
    private String namapropinsi;
    @SerializedName("favorited")
    @Expose
    private Integer favorited;

    /**
     * No args constructor for use in serialization
     *
     */
    public Channel() {
    }

    /**
     *
     * @param urlStreamStereo
     * @param pathlogo
     * @param frekuensi
     * @param kodepropinsi
     * @param kota
     * @param id
     * @param band
     * @param favorited
     * @param namapropinsi
     * @param name
     * @param urlStreamMono
     * @param siteaDd
     * @param statMono
     * @param statStereo
     * @param kategori
     */
    public Channel(Integer id, String name, String siteaDd, String urlStreamStereo, String urlStreamMono, Integer statMono, Integer statStereo, String pathlogo, String kota, String frekuensi, String band, String kategori, String kodepropinsi, String namapropinsi, Integer favorited) {
        this.id = id;
        this.name = name;
        this.siteAdd = siteaDd;
        this.urlStreamStereo = urlStreamStereo;
        this.urlStreamMono = urlStreamMono;
        this.statMono = statMono;
        this.statStereo = statStereo;
        this.pathlogo = pathlogo;
        this.kota = kota;
        this.frekuensi = frekuensi;
        this.band = band;
        this.kategori = kategori;
        this.kodepropinsi = kodepropinsi;
        this.namapropinsi = namapropinsi;
        this.favorited = favorited;
    }

    protected Channel(Parcel in) {
        name = in.readString();
        siteAdd = in.readString();
        urlStreamStereo = in.readString();
        urlStreamMono = in.readString();
        pathlogo = in.readString();
        kota = in.readString();
        frekuensi = in.readString();
        band = in.readString();
        kategori = in.readString();
        kodepropinsi = in.readString();
        namapropinsi = in.readString();
    }

    public static final Creator<Channel> CREATOR = new Creator<Channel>() {
        @Override
        public Channel createFromParcel(Parcel in) {
            return new Channel(in);
        }

        @Override
        public Channel[] newArray(int size) {
            return new Channel[size];
        }
    };

    /**
     *
     * @return
     * The id
     */
    public Integer getId() {
        return id;
    }

    /**
     *
     * @param id
     * The id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     *
     * @return
     * The name
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     * The name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return
     * The siteaDd
     */
    public String getSiteAdd() {
        return siteAdd;
    }

    /**
     *
     * @param siteaDd
     * The sitea_dd
     */
    public void setSiteAdd(String siteaDd) {
        this.siteAdd = siteaDd;
    }

    /**
     *
     * @return
     * The urlStreamStereo
     */
    public String getUrlStreamStereo() {
        return urlStreamStereo;
    }

    /**
     *
     * @param urlStreamStereo
     * The url_stream_stereo
     */
    public void setUrlStreamStereo(String urlStreamStereo) {
        this.urlStreamStereo = urlStreamStereo;
    }

    /**
     *
     * @return
     * The urlStreamMono
     */
    public String getUrlStreamMono() {
        return urlStreamMono;
    }

    /**
     *
     * @param urlStreamMono
     * The url_stream_mono
     */
    public void setUrlStreamMono(String urlStreamMono) {
        this.urlStreamMono = urlStreamMono;
    }

    /**
     *
     * @return
     * The statMono
     */
    public Integer getStatMono() {
        return statMono;
    }

    /**
     *
     * @param statMono
     * The stat_mono
     */
    public void setStatMono(Integer statMono) {
        this.statMono = statMono;
    }

    /**
     *
     * @return
     * The statStereo
     */
    public Integer getStatStereo() {
        return statStereo;
    }

    /**
     *
     * @param statStereo
     * The stat_stereo
     */
    public void setStatStereo(Integer statStereo) {
        this.statStereo = statStereo;
    }

    /**
     *
     * @return
     * The pathlogo
     */
    public String getPathlogo() {
        return pathlogo;
    }

    /**
     *
     * @param pathlogo
     * The pathlogo
     */
    public void setPathlogo(String pathlogo) {
        this.pathlogo = pathlogo;
    }

    /**
     *
     * @return
     * The kota
     */
    public String getKota() {
        return kota;
    }

    /**
     *
     * @param kota
     * The kota
     */
    public void setKota(String kota) {
        this.kota = kota;
    }

    /**
     *
     * @return
     * The frekuensi
     */
    public String getFrekuensi() {
        return frekuensi;
    }

    /**
     *
     * @param frekuensi
     * The frekuensi
     */
    public void setFrekuensi(String frekuensi) {
        this.frekuensi = frekuensi;
    }

    /**
     *
     * @return
     * The band
     */
    public String getBand() {
        return band;
    }

    /**
     *
     * @param band
     * The band
     */
    public void setBand(String band) {
        this.band = band;
    }

    /**
     *
     * @return
     * The kategori
     */
    public String getKategori() {
        return kategori;
    }

    /**
     *
     * @param kategori
     * The kategori
     */
    public void setKategori(String kategori) {
        this.kategori = kategori;
    }

    /**
     *
     * @return
     * The kodepropinsi
     */
    public String getKodepropinsi() {
        return kodepropinsi;
    }

    /**
     *
     * @param kodepropinsi
     * The kodepropinsi
     */
    public void setKodepropinsi(String kodepropinsi) {
        this.kodepropinsi = kodepropinsi;
    }

    /**
     *
     * @return
     * The namapropinsi
     */
    public String getNamapropinsi() {
        return namapropinsi;
    }

    /**
     *
     * @param namapropinsi
     * The namapropinsi
     */
    public void setNamapropinsi(String namapropinsi) {
        this.namapropinsi = namapropinsi;
    }

    /**
     *
     * @return
     * The favorited
     */
    public Integer getFavorited() {
        return favorited;
    }

    /**
     *
     * @param favorited
     * The favorited
     */
    public void setFavorited(Integer favorited) {
        this.favorited = favorited;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(siteAdd);
        dest.writeString(urlStreamStereo);
        dest.writeString(urlStreamMono);
        dest.writeString(pathlogo);
        dest.writeString(kota);
        dest.writeString(frekuensi);
        dest.writeString(band);
        dest.writeString(kategori);
        dest.writeString(kodepropinsi);
        dest.writeString(namapropinsi);
    }
}
