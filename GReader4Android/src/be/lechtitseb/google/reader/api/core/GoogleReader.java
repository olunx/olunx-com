
package be.lechtitseb.google.reader.api.core;

import be.lechtitseb.google.reader.api.model.authentication.AuthenticationManager;
import be.lechtitseb.google.reader.api.model.authentication.GoogleCredentials;
import be.lechtitseb.google.reader.api.model.exception.AuthenticationException;
import be.lechtitseb.google.reader.api.model.exception.GoogleReaderException;
import be.lechtitseb.google.reader.api.model.feed.FeedDescriptor;
import be.lechtitseb.google.reader.api.model.feed.ItemDescriptor;

//FIXME the methods javadoc are all copy/pasted from those in GoogleReaderDataProvider, I don't know how to fix this (and if it needs to or not)
//the problem is that I don't think I can use an interface (return types are different)
/**
 * Unofficial Google Reader API, can be used to manipulate a Google Reader
 * account. Once the user is authenticated, it is possible to retrieve his
 * feeds, get unread/starred/... items, add new subscriptions, create new tags
 * for feeds, ...
 */
public final class GoogleReader implements
		AuthenticationManager<GoogleCredentials> {
	private GoogleReaderDataProvider api;

	public GoogleReader() {
		api = new GoogleReaderDataProvider();
	}

	public GoogleReader(String username, String password) {
		api = new GoogleReaderDataProvider(username, password);
	}

	public GoogleReader(GoogleCredentials credentials) {
		api = new GoogleReaderDataProvider(credentials);
	}

	public void clearCredentials() {
		api.clearCredentials();
	}

	/* (non-Javadoc)
	 * @see be.lechtitseb.google.reader.api.model.authentication.AuthenticationManager#getCredentials()
	 */
	public GoogleCredentials getCredentials() {
		return api.getCredentials();
	}

	/* (non-Javadoc)
	 * @see be.lechtitseb.google.reader.api.model.authentication.AuthenticationManager#hasCredentials()
	 */
	public boolean hasCredentials() {
		return api.hasCredentials();
	}

	/* (non-Javadoc)
	 * @see be.lechtitseb.google.reader.api.model.authentication.AuthenticationManager#isAuthenticated()
	 */
	public boolean isAuthenticated() {
		return api.isAuthenticated();
	}

	/* (non-Javadoc)
	 * @see be.lechtitseb.google.reader.api.model.authentication.AuthenticationManager#login()
	 */
	public boolean login() throws AuthenticationException {
		return api.login();
	}

	/* (non-Javadoc)
	 * @see be.lechtitseb.google.reader.api.model.authentication.AuthenticationManager#logout()
	 */
	public void logout() {
		api.logout();
	}

	/* (non-Javadoc)
	 * @see be.lechtitseb.google.reader.api.model.authentication.AuthenticationManager#setCredentials(java.lang.Object)
	 */
	public void setCredentials(GoogleCredentials credentials) {
		api.setCredentials(credentials);
	}
	
	
	/**
	 * Export your subscription list to OPML
	 * @return The OPML String
	 * @throws GoogleReaderException If the user is not authenticated
	 */
	public String exportSubscriptionsToOPML() throws GoogleReaderException{
		return api.exportSubscriptionsToOPML();
	}
	
	
	/**
	 * Get an unread item (also marks it as READ!)
	 * @param token The shuffle token found in the user preferences (@see UserPreferences class)
	 * @return The next unread item (HTML page!)
	 * @throws GoogleReaderException If the user is not authenticated
	 */
	public String getNextUnreadItem(String shuffleToken) throws GoogleReaderException{
		return api.getNextUnreadItem(shuffleToken);
	}
	
	/**
	 * Mark all the items from a feed as read
	 * @param feedId The feed to mark as read
	 * @throws GoogleReaderException If the user is not authenticated
	 */
	public void markFeedAsRead(String feedId) throws GoogleReaderException{
		api.markFeedAsRead(feedId);
	}
	
	/**
	 * Mark all the items from a feed as read
	 * @param feed The feed to mark as read
	 * @throws GoogleReaderException If the user is not authenticated
	 */
	public void markFeedAsRead(FeedDescriptor feed) throws GoogleReaderException{
		api.markFeedAsRead(feed);
	}
	
	/**
	 * Mark the item from a feed as read
	 * @param feed The feed to mark as read
	 * @throws GoogleReaderException If the user is not authenticated
	 */
	public void markItemAsRead(ItemDescriptor item,FeedDescriptor feed) throws GoogleReaderException{
		api.markItemAsRead(item,feed);
	}
	
	/**
	 * @author maratische
	 * Mark the item from a feed as read
	 * @param feed The feed to mark as read
	 * @throws GoogleReaderException If the user is not authenticated
	 */
	public void markItemAsRead(String itemUrl,String feedId) throws GoogleReaderException{
		api.markItemAsRead(itemUrl,feedId);
	}
	
        public GoogleReaderDataProvider getApi () {
                return api;
        }
	//public void addSubscription()
}