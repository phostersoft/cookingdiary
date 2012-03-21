package com.phostersoft.cookingdiary.utils;

import android.content.Context;
import android.database.Cursor;
import android.widget.AlphabetIndexer;
import android.widget.SectionIndexer;
import android.widget.SimpleCursorAdapter;

public class IndexedCursorAdaptor extends SimpleCursorAdapter implements
		SectionIndexer {
	AlphabetIndexer alphaIndexer;
	
	public IndexedCursorAdaptor(Context context, int layout, Cursor c,
			String[] from, int[] to) {
		super(context, layout, c, from, to);
		alphaIndexer = new AlphabetIndexer(c, 1, 
				"ABCDEFGHIJKLMNOPQRSTUVWXYZ");
	}
	
	public int getPositionForSection(int arg0) {
		return alphaIndexer.getPositionForSection(arg0);
	}

	public int getSectionForPosition(int arg0) {
		return alphaIndexer.getSectionForPosition(arg0);
	}

	public Object[] getSections() {
		return alphaIndexer.getSections();
	}

}
