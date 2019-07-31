package com.box.androidsdk.share.vm

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import com.box.androidsdk.content.models.BoxItem
import com.box.androidsdk.share.internal.models.BoxFeatures
import com.box.androidsdk.share.utils.ShareSDKTransformer
import com.nhaarman.mockitokotlin2.mock
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TestRule

class ShareLinkVMTest {
    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()
    private val mShareLinkedItem: LiveData<PresenterData<BoxItem>> = mock()
    private val mSupportedFeatures: LiveData<PresenterData<BoxFeatures>> = mock()
    private val transformer: ShareSDKTransformer = mock()
    @Before
    fun setup() {

    }

}