package com.stellaris.stchat.view.listview;

import android.content.Context;
import android.widget.SectionIndexer;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;


class SectionIndexerAdapterWrapper extends
		AdapterWrapper implements SectionIndexer {
	
	SectionIndexer mSectionIndexerDelegate;

	SectionIndexerAdapterWrapper(Context context,
                                 StickyListHeadersAdapter delegate) {
		super(context, delegate);
		mSectionIndexerDelegate = (SectionIndexer) delegate;
	}

	@Override
	public int getPositionForSection(int section) {
		return mSectionIndexerDelegate.getPositionForSection(section);
	}

	@Override
	public int getSectionForPosition(int position) {
		return mSectionIndexerDelegate.getSectionForPosition(position);
	}

	@Override
	public Object[] getSections() {
		return mSectionIndexerDelegate.getSections();
	}

}
