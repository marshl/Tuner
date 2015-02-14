package com.example.tuner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;
import android.util.Xml;

import com.example.tuner.RadioMaster.SOUND_TYPE;

public class Radio
{
	public File directory;
	public String name = "UNDEFINED";
	public boolean songOverlay = false;
	
	public int currentStationIndex = 0;
	
	public ArrayList<Station> stationList = new ArrayList<Station>();
	public ArrayList<File> advertList = new ArrayList<File>();
	public ArrayList<File> weatherList = new ArrayList<File>();
	public ArrayList<File> newsList = new ArrayList<File>();
	
	public Radio( File _directory ) throws Exception
	{
		this.directory = _directory;
		
		this.loadXml();
		
		Collections.shuffle( this.advertList );
	}
	
	private void loadXml() throws Exception
	{
		File dataFile = new File( this.directory.toString() + "/stations.xml" );
		if ( !dataFile.exists() )
		{
			throw new IOException( "Could not find " + dataFile.toString() );
		}
		
		FileInputStream fileStream = new FileInputStream( dataFile );
	
		XmlPullParser parser = Xml.newPullParser();
		parser.setFeature( XmlPullParser.FEATURE_PROCESS_NAMESPACES, false );
		parser.setInput( fileStream, null );
		parser.nextTag();
		this.readStationList( parser );
		
		fileStream.close();
	}
	
	private void readStationList( XmlPullParser _parser ) throws Exception
	{
		_parser.require( XmlPullParser.START_TAG, null, "stations" );

		this.name = _parser.getAttributeValue( null, "name" );
		String overlayStr = _parser.getAttributeValue( null, "songoverlay" );
		this.songOverlay = overlayStr.equals( "true" );
		
		while ( _parser.next() != XmlPullParser.END_TAG )
		{
			if ( _parser.getEventType() != XmlPullParser.START_TAG )
			{
				continue;
			}

			String name = _parser.getName();
			if ( name.equals( "adverts" ) )
			{
				String dir = _parser.getAttributeValue( null, "dir" );
				this.readOtherSoundGroup( this.advertList, dir, "adverts.xml", "adverts", "advert");
				RadioMaster.skip( _parser );
			}
			else if ( name.equals( "weather" ) )
			{
				String dir = _parser.getAttributeValue( null, "dir" );
				this.readOtherSoundGroup( this.weatherList, dir, "weather.xml", "weather", "weather");
				RadioMaster.skip( _parser );
			}
			else if ( name.equals( "news" ) )
			{
				String dir = _parser.getAttributeValue( null, "dir" );
				this.readOtherSoundGroup( this.newsList, dir, "news.xml", "news", "news");
				RadioMaster.skip( _parser );
			}
			else if ( name.equals( "station" ) )
			{
				String dir = _parser.getAttributeValue( null, "dir" );
				try
				{
					Station station = new Station( this, dir );
					this.stationList.add( station );
				}
				catch ( Exception _e )
				{
					Log.w( "TNR", _e.toString() );
				}
				RadioMaster.skip( _parser );
			}
			else if ( name.equals( "format" ) )
			{
				RadioMaster.skip( _parser );
			}
			else
			{
				RadioMaster.skip( _parser );
			}
		}
	}
	
	//private void readAdverts( String _path ) throws IOException, XmlPullParserException
	private void readOtherSoundGroup( List<File> _fileList, String _folderName, String _filename, String _rootName, String _elementName ) throws IOException, XmlPullParserException
	{
		//this.advertPath = new File( this.directory.toString() + "/" + _path );
		String rootPath = this.directory.toString() + "/" + _folderName;
		//File dataFile = new File( this.advertPath.toString() + "/adverts.xml" );
		File dataFile = new File( rootPath + "/" + _filename );
		if ( !dataFile.exists() )
		{
			throw new IOException( "Could not find " + dataFile.toString() );
		}
		
		FileInputStream fileStream = new FileInputStream( dataFile );
	
		XmlPullParser parser = Xml.newPullParser();
		parser.setFeature( XmlPullParser.FEATURE_PROCESS_NAMESPACES, false );
		parser.setInput( fileStream, null );
		parser.nextTag();
		//this.readAdvertList( parser );
		this.readOtherSoundList( _fileList, parser, rootPath, _rootName, _elementName );
		
		fileStream.close();
	}

	private void readOtherSoundList( List<File> _list, XmlPullParser _parser, String _rootPath, String _rootName, String _elementName ) throws XmlPullParserException, IOException
	//private void readAdvertList( XmlPullParser _parser ) throws XmlPullParserException, IOException
	{
		_parser.require( XmlPullParser.START_TAG, null, _rootName );
		//_parser.require( XmlPullParser.START_TAG, null, "adverts" );

		while ( _parser.next() != XmlPullParser.END_TAG )
		{
			if ( _parser.getEventType() != XmlPullParser.START_TAG )
			{
				continue;
			}

			String name = _parser.getName();
			if ( name.equals( _elementName ) )
			//if ( name.equals( "advert" ) )
			{
				String filename = _parser.getAttributeValue( null, "file" );
				File file = new File( _rootPath + "/" + filename );
				//this.advertList.add( file );
				_list.add( file );
				//Log.d( "TNR", filename );
				RadioMaster.skip( _parser );
			}
			else
			{
				RadioMaster.skip( _parser );
			}
		}
	}

	public Station getCurrentStation()
	{
		return this.stationList.get( this.currentStationIndex  );
	}

	

	public boolean canPlaySoundType( SOUND_TYPE _soundType )
	{
		Station station = this.getCurrentStation();
		switch ( _soundType )
		{
		case ADVERT:
			return this.advertList.size() > 0;
		case GENERAL:
			return station.miscFileMap.get( SOUND_TYPE.GENERAL ).size() > 0;
		case ID:
			return station.miscFileMap.get( SOUND_TYPE.ID ).size() > 0;
		case NEWS:
			return this.newsList.size() > 0;
		case SONG:
			return station.songList.size() > 0;
		case TIME:
			return station.miscFileMap.get( SOUND_TYPE.TIME ).size() > 0;
		case WEATHER:
			return station.miscFileMap.get( SOUND_TYPE.WEATHER ).size() > 0
				|| this.weatherList.size() > 0;
		case TO_WEATHER:
		case TO_NEWS:
		case TO_ADVERT:
			return false;
		}
		return false;
	}

	public File getNextAdvert()
	{
		File advert = this.advertList.get( 0 );
		this.advertList.remove( 0 );
		this.advertList.add( advert );
		return advert;
	}
	
	public File getNextNews()
	{
		File news = this.newsList.get( 0 );
		this.newsList.remove( 0 );
		this.newsList.add( news );
		return news;
	}
	
	public File getNextWeather()
	{
		File weather = this.weatherList.get( 0 );
		this.weatherList.remove( 0 );
		this.weatherList.add( weather );
		return weather;
	}

}
