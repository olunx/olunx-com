package com.olunx.util;

public class Word {

	private String word = "";// 单词
	private String phonetic = "";// 音标
	private String translation = "";// 词义
	private String pronounce = "";// 发音
	private String sentences = "";// 短语和短语翻译

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public String getPhonetic() {
		return phonetic;
	}

	public void setPhonetic(String phonetic) {
		this.phonetic = phonetic;
	}

	public String getTranslation() {
		return translation;
	}

	public void setTranslation(String translation) {
		this.translation = translation;
	}

	public String getPronounce() {
		return pronounce;
	}

	public void setPronounce(String pronounce) {
		this.pronounce = pronounce;
	}

	public String getSentences() {
		return sentences;
	}

	public void setSentences(String sentences) {
		this.sentences = sentences;
	}

}
