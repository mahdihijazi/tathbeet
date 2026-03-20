package com.quran.tathbeet.sync

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthLinkResolverTest {

    @Test
    fun resolves_plain_https_link_as_is() {
        val link = "https://example.com/__/auth/action?mode=signIn&oobCode=abc"

        assertEquals(link, resolveAuthLink(link))
    }

    @Test
    fun unwraps_firebase_redirect_link() {
        val inner = "https://example.com/__/auth/action?mode=signIn&oobCode=abc"
        val wrapper = "https://example.com/__/auth/links?link=https%3A%2F%2Fexample.com%2F__%2Fauth%2Faction%3Fmode%3DsignIn%26oobCode%3Dabc"

        assertEquals(inner, resolveAuthLink(wrapper))
    }

    @Test
    fun resolves_custom_tathbeet_scheme() {
        val inner = "https://example.com/__/auth/action?mode=signIn&oobCode=abc"
        val link = "tathbeet://auth?emailLink=https%3A%2F%2Fexample.com%2F__%2Fauth%2Faction%3Fmode%3DsignIn%26oobCode%3Dabc"

        assertEquals(inner, resolveAuthLink(link))
    }

    @Test
    fun returns_null_for_unsupported_scheme() {
        assertNull(resolveAuthLink("mailto:owner@example.com"))
    }
}
