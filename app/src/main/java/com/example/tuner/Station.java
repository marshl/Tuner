package com.example.tuner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.Xml;

import com.example.tuner.RadioMaster.SOUND_TYPE;

public class Station
{
	private Radio  parentRadio;
	private File   directory;
	public String name;
	public String dj;
	public String genre;
	private File   iconFile;
	public Bitmap iconData;
	public boolean fullTrack = false;
	
	private ArrayList<Song> songList = new ArrayList<Song>();
    private int songIndex = 0;
	public HashMap<SOUND_TYPE, ArrayList<File>> miscFileMap = new HashMap<SOUND_TYPE, ArrayList<File>>();
	
	public Station( Radio _parentRadio, String _dir ) throws Exception
	{
		this.parentRadio = _parentRadio;
		this.directory = new File( this.parentRadio.directory.toString() + "/" + _dir );
		if ( !this.directory.exists() )
		{
			throw new IOException( "Station directory \"" + this.directory.toString() + "\" not found" );
		}
		
		this.miscFileMap.put( SOUND_TYPE.GENERAL, new ArrayList<File>() );
		this.miscFileMap.put( SOUND_TYPE.ID,      new ArrayList<File>() );
		this.miscFileMap.put( SOUND_TYPE.TIME,    new ArrayList<File>() );
		this.miscFileMap.put( SOUND_TYPE.WEATHER, new ArrayList<File>() );
		
		this.miscFileMap.put( SOUND_TYPE.TO_ADVERT, new ArrayList<File>() );
		this.miscFileMap.put( SOUND_TYPE.TO_WEATHER, new ArrayList<File>() );
		this.miscFileMap.put( SOUND_TYPE.TO_NEWS, new ArrayList<File>() );
		
		this.loadXml();
        Random rand = new Random();
        this.songIndex = rand.nextInt( this.songList.size() ) ;

		for ( Entry<SOUND_TYPE, ArrayList<File>> entry : this.miscFileMap.entrySet() )
		{
			Collections.shuffle( entry.getValue() );
		}
	}
	
	private void loadXml() throws Exception
	{
		File dataFile = new File( this.directory.toString() + "/station.xml" );
		if ( !dataFile.exists() )
		{
			throw new IOException( "Could not find " + dataFile.toString() );
		}
		
		FileInputStream fileStream = new FileInputStream( dataFile );
	
		XmlPullParser parser = Xml.newPullParser();
		parser.setFeature( XmlPullParser.FEATURE_PROCESS_NAMESPACES, false );
		parser.setInput( fileStream, null );
		parser.nextTag();
		this.readStationData( parser );
		
		fileStream.close();
	}
	
	private void readStationData( XmlPullParser _parser ) throws Exception
	{
		_parser.require( XmlPullParser.START_TAG, null, "station" );

		this.name = _parser.getAttributeValue( null, "name" );
		this.dj = _parser.getAttributeValue( null, "dj" );
		this.genre = _parser.getAttributeValue( null, "genre" );
		
		String iconName = _parser.getAttributeValue( null, "icon" );
		if ( iconName != null )
		{
			this.iconFile = new File( this.directory + "/" + iconName );
			
			File pngFile = new File( this.iconFile.toString().replaceFirst( ".bmp", ".png" ) );
			//Log.w( "TNR", pngFile.toString() );
			if ( pngFile.isFile() )
			{
				this.iconData = BitmapFactory.decodeFile( pngFile.toString() );
			}
			else
			{
				this.iconData = BitmapFactory.decodeFile( iconFile.toString() );
			}
		}
		
		String fullTrackStr = _parser.getAttributeValue( null, "fulltrack" );
		this.fullTrack = fullTrackStr != null && fullTrackStr.equals( "true" );
		
		while ( _parser.next() != XmlPullParser.END_TAG )
		{
			if ( _parser.getEventType() != XmlPullParser.START_TAG )
			{
				continue;
			}

			String name = _parser.getName();
			
			if ( name.equals( "general" )
			  || name.equals( "weather" )
			  || name.equals( "id" )
		      || name.equals( "time" ) 
		      || name.equals( "to_ad" )
		      || name.equals( "to_news" )
		      || name.equals( "to_weather" ) )
			{
				String filename = _parser.getAttributeValue( null, "file" );
				File file = new File( this.directory.toString() + "/" + filename );
				//Log.d( "TNR", file.toString() );
				
				if ( !RadioMaster.instance.checkFile( file ) )
				{
					RadioMaster.skip( _parser );
					continue;
				}
				
				if ( name.equals( "general" ) )
				{
					this.miscFileMap.get( SOUND_TYPE.GENERAL ).add( file );
				}
				else if ( name.equals( "weather" ) )
				{
					this.miscFileMap.get( SOUND_TYPE.WEATHER ).add( file );
				}
				else if ( name.equals( "id" ) )
				{
					this.miscFileMap.get( SOUND_TYPE.ID ).add( file );
				}
				else if ( name.equals( "time" ) )
				{
					this.miscFileMap.get( SOUND_TYPE.TIME ).add( file );
				}
				else if ( name.equals( "to_ad" ) )
				{
					this.miscFileMap.get( SOUND_TYPE.TO_ADVERT ).add( file );
				}
				else if ( name.equals( "to_news" ) )
				{
					this.miscFileMap.get( SOUND_TYPE.TO_NEWS ).add( file );
				}
				else if ( name.equals( "to_weather" ) )
				{
					this.miscFileMap.get( SOUND_TYPE.TO_WEATHER ).add( file );
				}
				RadioMaster.skip( _parser );
			}
			else if ( name.equals( "song" ) )
			{
				this.readSong( _parser );
			}
			else
			{
				RadioMaster.skip( _parser );
			}
		}
	}
	
	private void readSong( XmlPullParser _parser ) throws XmlPullParserException, IOException
	{
		_parser.require( XmlPullParser.START_TAG, null, "song" );

		Song song = new Song();
		
		song.name = _parser.getAttributeValue( null, "name" );
		song.artist = _parser.getAttributeValue( null, "artist" );
		
		//Log.d( "TNR", "SongName: " + song.name );
		
		while ( _parser.next() != XmlPullParser.END_TAG )
		{
			if ( _parser.getEventType() != XmlPullParser.START_TAG )
			{
				continue;
			}

			String name = _parser.getName();
			
			if ( name.equals( "in" )
			  || name.equals( "out" ) 
			  || name.equals( "main" ) )
			{
				String dir = _parser.getAttributeValue( null, "file" );
				
				File file = new File( this.directory.toString() + "/" + dir );
				//Log.d( "TNR", "Song Element " + name + " found: " + dir );
				if ( !RadioMaster.instance.checkFile( file ) )
				{
					RadioMaster.skip( _parser );
					continue;
				}
				
				if ( name.equals( "in" ) )
				{
					song.introList.add( file );
				}
				else if ( name.equals( "out" ) )
				{
					song.outroList.add( file );
				}
				else
				{
					song.main = file;
				}
				RadioMaster.skip( _parser );
			}
			else
			{
				RadioMaster.skip( _parser );
			}
		}
		this.songList.add( song );
	}
	
	public Song getNextSong()
	{
        Song song = this.getSongAtIndex( this.songIndex );
        this.songIndex = (this.songIndex + 1 >= this.songList.size() ) ? 0 : this.songIndex + 1;
        return song;
	}
	
	public File getNextMiscFile( SOUND_TYPE _soundType )
	{
		ArrayList<File> list = this.miscFileMap.get( _soundType );
		
		if ( list.isEmpty() )
		{
			return null;
		}
		
		File file = list.get( 0 );
		list.remove( 0 );
		list.add( file );
		return file;
	}

    public boolean hasSong()
    {
        return !this.songList.isEmpty();
    }

    public int getSongCount()
    {
        return this.songList.size();
    }

    public Song getSongAtIndex( int _position )
    {
        Log.d("TNR", this.songList.size() + ":" + _position );
        return this.songList.get( _position );
    }

    public void setSongIndex( int _songIndex )
    {
        this.songIndex = _songIndex;
    }
}
