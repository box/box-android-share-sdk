package com.box.androidsdk.share.vm

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class ActionbarTitleVMTest {
    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    private val actionbarTitleVM = ActionbarTitleVM()
    private val title = "Title"
    private val subtitle = "Subtitle"

    @Test
    fun `test title change update livedata correctly`() {
        assertNull(actionbarTitleVM.mTitle.value)

        actionbarTitleVM.setTitle(title)

        assertEquals(title, actionbarTitleVM.mTitle.value)
    }

    @Test
    fun `test subtitle change update livedata correctly`() {
        assertNull(actionbarTitleVM.mSubtitle.value)

        actionbarTitleVM.setSubtitle(subtitle)

        assertEquals(subtitle, actionbarTitleVM.mSubtitle.value)
    }
}