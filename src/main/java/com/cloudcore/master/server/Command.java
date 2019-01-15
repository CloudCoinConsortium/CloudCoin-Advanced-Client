package com.cloudcore.master.server;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Command {

    public String commandName;

    /* JSON Fields */

    @Expose
    @SerializedName("command")
    public String command;
    @Expose
    @SerializedName("account")
    public String account;
    @Expose
    @SerializedName("amount")
    public Integer amount;
    /**
     * 0, 1, 2, 3: multiple notes, stack, jpegs, or CSV
     */
    @Expose
    @SerializedName("type")
    public Integer type;
    @Expose
    @SerializedName("tag")
    public String tag;

    public String filename;

    @Expose
    @SerializedName("toPath")
    public String toPath;

    @Expose
    @SerializedName("cloudcoin")
    public String cloudcoin;
    @Expose
    @SerializedName("passphrase")
    public String passphrase;

    @Expose
    @SerializedName("language")
    public String language;

    @Expose
    @SerializedName("ones")
    public String ones;
    @Expose
    @SerializedName("fives")
    public String fives;
    @Expose
    @SerializedName("twentyfives")
    public String twentyfives;
    @Expose
    @SerializedName("hundreds")
    public String hundreds;
    @Expose
    @SerializedName("twohundredfifties")
    public String twohundredfifties;
}
