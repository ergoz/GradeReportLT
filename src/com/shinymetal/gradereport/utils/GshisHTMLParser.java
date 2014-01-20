package com.shinymetal.gradereport.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.util.Log;

import com.shinymetal.gradereportlt.BuildConfig;
import com.shinymetal.gradereport.objects.GradeRec;
import com.shinymetal.gradereport.objects.GradeSemester;
import com.shinymetal.gradereport.objects.Lesson;
import com.shinymetal.gradereport.objects.MarkRec;
import com.shinymetal.gradereport.objects.Pupil;
import com.shinymetal.gradereport.objects.Schedule;
import com.shinymetal.gradereport.objects.TS;
import com.shinymetal.gradereport.objects.Week;

public class GshisHTMLParser {

	private final static String WHITESCPACE_CHARS = "" /*
											 * dummy empty string for
											 * homogeneity
											 */
			+ "\\u0009" // CHARACTER TABULATION
			+ "\\u000A" // LINE FEED (LF)
			+ "\\u000B" // LINE TABULATION
			+ "\\u000C" // FORM FEED (FF)
			+ "\\u000D" // CARRIAGE RETURN (CR)
			+ "\\u0020" // SPACE
			+ "\\u0085" // NEXT LINE (NEL)
			+ "\\u00A0" // NO-BREAK SPACE
			+ "\\u1680" // OGHAM SPACE MARK
			+ "\\u180E" // MONGOLIAN VOWEL SEPARATOR
			+ "\\u2000" // EN QUAD
			+ "\\u2001" // EM QUAD
			+ "\\u2002" // EN SPACE
			+ "\\u2003" // EM SPACE
			+ "\\u2004" // THREE-PER-EM SPACE
			+ "\\u2005" // FOUR-PER-EM SPACE
			+ "\\u2006" // SIX-PER-EM SPACE
			+ "\\u2007" // FIGURE SPACE
			+ "\\u2008" // PUNCTUATION SPACE
			+ "\\u2009" // THIN SPACE
			+ "\\u200A" // HAIR SPACE
			+ "\\u2028" // LINE SEPARATOR
			+ "\\u2029" // PARAGRAPH SEPARATOR
			+ "\\u202F" // NARROW NO-BREAK SPACE
			+ "\\u205F" // MEDIUM MATHEMATICAL SPACE
			+ "\\u3000" // IDEOGRAPHIC SPACE
	;
	/* A \s that actually works for Java’s native character set: Unicode */
	private final static String WHITESPACE_CHARCLASS = "[" + WHITESCPACE_CHARS + "]";
	/* A \S that actually works for Java’s native character set: Unicode */
	@SuppressWarnings("unused")
	private final static String NOT_WHITESPACE_CHARCLASS = "[^" + WHITESCPACE_CHARS	+ "]";
	
	private final static Pattern WHITESPACES_ONLY = Pattern.compile("^" + WHITESPACE_CHARCLASS +"+$");
	private final static Pattern SUBJECT_NAME = Pattern.compile("^[0-9]{1}\\." + WHITESPACE_CHARCLASS + "{1}.*");	
	private static ArrayList<MarkRec> mNewMarks = new ArrayList<MarkRec> ();
	
	public final static ArrayList<MarkRec> getNewMarks() {
		
		return mNewMarks;
	}
	
	public static Pupil getSelectedPupil(Document doc) throws ParseException {

		boolean found = false;
		Pupil p, selectedP = null;

		Elements pupilSelectors = doc.getElementsByAttributeValue("id",
				"ctl00_topMenu_pupil_drdPupils");
		for (Element pupilSelector : pupilSelectors) {

			Elements pupils = pupilSelector.getAllElements();
			for (Element pupil : pupils) {
				if (pupil.tagName().equals("option")) {

					String value = pupil.attr("value");

					found = true;
					if ((p = Pupil.getByFormId(value)) == null) {

						p = new Pupil(pupil.text(), value);
						long rowId = p.insert();
						
						if (BuildConfig.DEBUG)
							Log.d("GshisHTMLParser", TS.get()
									+ " Pupil.insert() = " + rowId);
					}

					if (pupil.hasAttr("selected")
							&& pupil.attr("selected").equals("selected")) {

						selectedP = p;
					}
				}
			}
		}

		if (!found) {

			if (BuildConfig.DEBUG)
				Log.d("GshisParser", TS.get() + " Alternative fields found!");

			Element userName = doc.getElementsByClass("user-name").first();
			Element userId = doc.getElementsByAttributeValue("id",
					"ctl00_topMenu_tbUserId").first();

			String name = userName.text();
			String id = userId.attr("value");

			if (BuildConfig.DEBUG)
				Log.d("GshisParser", TS.get() + " name=" + name + " id=" + id);

			if ((p = Pupil.getByFormId(id)) == null) {

				p = new Pupil(name, id);
				long rowId = p.insert();

				if (BuildConfig.DEBUG)
					Log.d("GshisParser", TS.get() + " Pupil.insert() = "
							+ rowId);
			}

			selectedP = p;
		}

		if (selectedP == null)

			throw new ParseException("Pupils not found", 0);
		
		return selectedP;
	}

	public static Schedule getSelectedSchedule(Document doc, Pupil selPupil) throws ParseException {

		boolean found = false;
		Schedule selectedS = null;

		Elements yearSelectors = doc.getElementsByAttributeValue("id",
				"ctl00_learnYear_drdLearnYears");
		for (Element yearSelector : yearSelectors) {

			Elements years = yearSelector.getAllElements();
			for (Element year : years) {
				if (year.tagName().equals("option")) {

					String value = year.attr("value");
					Schedule schedule;

					found = true;
					
					if ((schedule = selPupil.getScheduleByFormId(value)) == null) {

						final SimpleDateFormat f = new SimpleDateFormat(
								"yyyy dd.MM", Locale.ENGLISH);
						f.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
						schedule = new Schedule(value, year.text());

						Date start = f.parse(year.text().substring(0,
								year.text().indexOf("-") - 1)
								+ " 01.09");
						Date stop = f.parse(year.text().substring(
								year.text().indexOf("-") + 2,
								year.text().length())
								+ " 31.05");
						
						schedule.setStart(start);
						schedule.setStop(stop);
				    	
						selPupil.addSchedule(schedule);
					}

					if (year.hasAttr("selected")
							&& year.attr("selected").equals("selected")) {

						selectedS = schedule;
					}
				}
			}
		}

		if (!found)
			throw new ParseException("Years not found", 0);
		
		return selectedS;
	}

	public static Week getSelectedWeek(Document doc, Schedule s) throws ParseException {

		boolean found = false;
		Week selectedW = null;
		
		SimpleDateFormat f = new SimpleDateFormat("yyyy dd.MM", Locale.ENGLISH);
		f.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
		
		Elements weekSelectors = doc.getElementsByAttributeValue("id",
				"ctl00_body_week_drdWeeks");
		for (Element weekSelector : weekSelectors) {

			Elements weeks = weekSelector.getAllElements();
			for (Element week : weeks) {
				if (week.tagName().equals("option")) {

					String value = week.text();
					Week w;
					found = true;
					
					if ((w = s.getWeek(week.attr("value"))) == null ) {

						w = new Week();
						
						String wBegin = value.substring(0, value.indexOf("-") - 1);
						String wMonth = wBegin.substring(wBegin.indexOf(".") + 1, wBegin.length());

						String year;
						if (Integer.parseInt(wMonth) > 7) {
							year = s.getFormText().substring(0, s.getFormText().indexOf("-") - 1);
						} else {
							year = s.getFormText().substring(s.getFormText().indexOf("-") + 2,
									s.getFormText().length());
						}

						w.setStart(f.parse(year	+ " " + wBegin));
						w.setFormText(week.text());
						w.setFormId(week.attr("value"));

						s.addWeek(w);
					}

					if (week.hasAttr("selected")
							&& week.attr("selected").equals("selected")) {
						
						selectedW = w;
						long u = w.setLoaded().update();
						
						if (BuildConfig.DEBUG)
							Log.d("GshisHTMLParser", TS.get()
									+ " Week.update() = " + u);
					}
				}
			}
		}

		if (!found)
			throw new ParseException("Weeks not found", 0);
		
		return selectedW;
	}

	public static void getLessons(Document doc, Schedule s) throws ParseException {

		final SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ENGLISH);
		format.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
		
		Elements lessonCells = doc.getElementsByAttribute("number");
		
		for (Element lessonCell : lessonCells) {

			Lesson l, lPrev = null;  // lPrev to handle duplicate lesson
			int sameLesson = 0;      // Also to handle duplicate lesson
			
			int number = Integer.parseInt(lessonCell.attr("number"));
			String time = "";

			Elements timeDetails = lessonCell
					.getElementsByClass("cell-header2");
			for (Element timeDetail : timeDetails) {
				if (timeDetail.hasAttr("style"))
					time = timeDetail.text();
			}

			Elements lessonCellDetails = lessonCell
					.getElementsByAttribute("jsdate");
			for (Element lessonCellDetail : lessonCellDetails) {

				String date = lessonCellDetail.attr("jsdate");				
				int index = 0;
				sameLesson = 0;
				
				for (Element subject : lessonCellDetail
						.getElementsByAttributeValue("class", "lesson-subject")) {

					if (subject == null || subject.text() == null
							|| subject.text().length() <= 0) {
						// No lesson scheduled
						continue;
					}

					Date start = format.parse(date + " " + time.substring(0, time.indexOf("-") - 1));
					if ((l = s.getLessonByNumber(start, number)) == null) {
						
						if (BuildConfig.DEBUG)
							Log.d("GshisHTMLParser", TS.get()
									+ " getLessons() not found in db, will insert");

						l = new Lesson();
						sameLesson = 0;

						l.setStart(start);
						l.setStop(format.parse(date
								+ " "
								+ time.substring(time.indexOf("-") + 2,
										time.length())));
						l.setFormId(subject.attr("id"));
						l.setFormText(subject.text());
						l.setTeacher(lessonCellDetail
								.getElementsByAttributeValue("class",
										"lesson-teacher").get(sameLesson).text());
						l.setNumber(number);

						s.addLesson(l);

					} else {
						
						if (BuildConfig.DEBUG)
							Log.d("GshisHTMLParser", TS.get()
									+ " getLessons() found in db, will update");
						
						l.setFormId(subject.attr("id"));
						
						if (lPrev != null
								&& lPrev.getStart().equals(start)
								&& lPrev.getNumber() == number) {
							
							if (BuildConfig.DEBUG)
								Log.d("GshisHTMLParser", TS.get()
										+ " getLessons() dup = " + subject.text() + " index = " + index + " sameLesson = " + sameLesson);

							
							sameLesson++;
							
							if (!lPrev.getFormText().equals(subject.text()))								
								l.setFormText(fixDuplicateString(
										subject.text(), lPrev.getFormText(), sameLesson));
							
							String teacher = lessonCellDetail
									.getElementsByAttributeValue("class",
											"lesson-teacher").get(index).text();
							
							if (!lPrev.getTeacher().equals(teacher))								
								l.setTeacher(fixDuplicateString(
										teacher, lPrev.getTeacher(), sameLesson));

						} else {

							l.setNumber(number);
							l.setFormText(subject.text());
							l.setTeacher(lessonCellDetail
									.getElementsByAttributeValue("class",
											"lesson-teacher").get(index).text());
						}
						
						l.update();
					}
					
					lPrev = l;
					index++;
				}
			}
		}
	}

	public static void getLessonsDetails(Document doc, Schedule s)
			throws ParseException {

		final SimpleDateFormat fmt = new SimpleDateFormat("dd.MM.yyyy",
				Locale.ENGLISH);
		fmt.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
		
		Elements tableCells = doc.getElementsByAttributeValue("class",
				"table diary");
		for (Element tableCell : tableCells) {

			int tdCount = 0;
			Date date = null;
			Lesson l, lPrev = null; // lPrev to handle duplicate lesson
			int sameLesson = 0;      // Also to handle duplicate lesson

			Elements trs = tableCell.getElementsByTag("tr");
			for (Element tr : trs) {

				if (tr.hasAttr("class")
						&& tr.attr("class").equals("table-header"))
					continue;

				l = null;
				sameLesson = 0; // assume no bug here
				Elements tds = tr.getElementsByTag("td");

				for (Element td : tds) {

					if (td.hasAttr("class") && td.attr("class").equals("date")) {

						date = fmt.parse(td.getElementsByTag("div").first().text());
						tdCount = 1;

					} else if (td.hasAttr("class")
							&& td.attr("class").equals("diary-mark")) {

						String marks = fetchLongCellStringNoWhitespaces(td);
						if (l != null && marks != null) {
							
							if (sameLesson > 0 && lPrev != null) {
								
								l.setMarks(fixDuplicateString(marks, lPrev.getMarks(), sameLesson));
							} else
								l.setMarks(marks);
						}
						tdCount++;

					} else if (td.hasAttr("class")
							&& td.attr("class").equals("diary-comment")) {

						String comment = fetchLongCellStringNoWhitespaces(td);
						if (l != null && comment != null) {
							
							if (sameLesson > 0 && lPrev != null) {
								
								l.setComment(fixDuplicateString(comment, lPrev.getComment(), sameLesson));
							} else
								l.setComment(comment);							
						}
						
						tdCount++;

					} else if (tdCount == 2) {

						String theme = fetchLongCellStringNoWhitespaces(td);
						if (l != null && theme != null) {
							
							if (sameLesson > 0 && lPrev != null) {
								
								l.setTheme(fixDuplicateString(theme, lPrev.getTheme(), sameLesson));
							} else
								l.setTheme(theme);
						}
						tdCount++;

					} else if (tdCount == 3) {

						String homework = fetchLongCellStringNoWhitespaces(td);
						if (l != null && homework != null) {
							
							if (sameLesson > 0 && lPrev != null) {
								
								l.setHomework(fixDuplicateString(homework, lPrev.getHomework(), sameLesson));
							} else
								l.setHomework(homework);
						}
						tdCount++;

					} else if (SUBJECT_NAME.matcher(td.text()).find()) {

						tdCount = 2;
						int number = Integer.parseInt(td.text().substring(0, 1)); 
						l = s.getLessonByNumber(date, number);

						if (lPrev != null && l != null
								&& l.getStart().equals(lPrev.getStart())
								&& l.getNumber() == lPrev.getNumber()) {

							// We hit the same lesson bug
							sameLesson++;
						}

					} else {
						tdCount++;
					}
				}
				
				if (l != null) {
					lPrev = l;
					l.update();
				}
			}
		}
	}

	public static String fetchLongCellString(Element e) {

		for (Element link : e.getElementsByTag("a")) {

			if (link.hasAttr("txttitle")) {
				return link.attr("txttitle");
			}
		}
		return e.text();
	}

	public static boolean containsPrintableChars (String str) {

		if (str == null || str.length() <= 0)
			return false;

		Matcher matcher = WHITESPACES_ONLY.matcher(str.replaceAll("&nbsp;", " "));

		if (matcher.find())
			return false;
		
		return true;
	}
	
	public static String fetchLongCellStringNoWhitespaces(Element e) {

		String s = fetchLongCellString(e).replaceAll("&nbsp;", " ");
		Matcher matcher = WHITESPACES_ONLY.matcher(s);

		if (matcher.find())
			return null;

		return s;
	}

	public static GradeSemester getActiveGradeSemester(Document doc, Schedule sch)
			throws ParseException {
		
		boolean found = false;
		GradeSemester selG = null;
		
		SimpleDateFormat fmt = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
		fmt.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
		
		Elements semesterSelectors = doc.getElementsByAttributeValue("id",
				"ctl00_body_drdTerms");
		for (Element semesterSelector : semesterSelectors) {

			Elements semesters = semesterSelector.getAllElements();
			for (Element semester : semesters) {
				if (semester.tagName().equals("option")) {

					String value = semester.text();
					GradeSemester sem;
					found = true;

					if ((sem = sch.getSemester(semester.attr("value"))) == null ) {
						
						sem = new GradeSemester ();

						sem.setStart(fmt.parse(value.substring(12, value.indexOf("-") - 1)));
						sem.setStop(fmt.parse(value.substring(value.indexOf("-") + 2, value.length() - 2)));
						sem.setFormText(semester.text());
						sem.setFormId(semester.attr("value"));

						sch.addSemester(sem);
					}

					if (semester.hasAttr("selected")
							&& semester.attr("selected").equals("selected")) {

						long u = sem.setLoaded().update();			
						selG = sem;
						
						if (BuildConfig.DEBUG)
							Log.d("GshisHTMLParser", TS.get()
									+ " Semester.update() = " + u);
					}
				}
			}
		}

		if (!found)
			throw new ParseException("Semesters not found", 0);
		
		return selG;
	}

	public static void getGrades(Document doc, Schedule sch, GradeSemester s)
			throws ParseException {
		
		mNewMarks = new ArrayList<MarkRec> ();
		
		Elements tableCells = doc.getElementsByAttributeValue("class",
				"table rating");
		for (Element tableCell : tableCells) {

			Elements trs = tableCell.getElementsByTag("tr");
			for (Element tr : trs) {

				if (tr.hasAttr("class")
						&& tr.attr("class").equals("table-header"))
					continue;
				
				GradeRec rec = new GradeRec();
				int thCount = 0;

				Elements ths = tr.getElementsByTag("th");
				for (Element th : ths) {

					if (th.hasAttr("class")
							&& th.attr("class").equals("table-header3")) {
						
						rec.setFormText(th.text());
						thCount = 2;

					} else if (th.hasAttr("class")
							&& th.attr("class").equals("cell-header2")) {
						
						switch (thCount) {
						case 2:
							if (containsPrintableChars(th.text()))
								rec.setAbsent(Integer.parseInt(th.text()));
							break;
						case 3:
							if (containsPrintableChars(th.text()))
								rec.setReleased(Integer.parseInt(th.text()));
							break;
						case 4:
							if (containsPrintableChars(th.text()))
								rec.setSick(Integer.parseInt(th.text()));
							break;
						case 5:
							if (containsPrintableChars(th.text()))
								rec.setAverage(Float.parseFloat(th.text().replace(',', '.')));
							break;
						}

						thCount++;
					}
				}

				Element total = tr.getElementsByTag("td").last();
				if (containsPrintableChars(total.text()) && total.text().matches("[-+]?\\d*\\.?\\d+")) {

					rec.setTotal(Integer.parseInt(total.text()));
				}

				rec.setStart(s.getStart());
				rec.setStop(s.getStop());
				
				if (containsPrintableChars(rec.getFormText())) {

					GradeRec exR = sch.getGradeRecByDateText (rec.getStart(), rec.getFormText());
					if (exR != null) {
						
//						if (BuildConfig.DEBUG)
//							Log.d("GshisHTMLParser",
//									TS.get()
//											+ " before update GradeRec, start = "
//											+ exR.getStart() + " stop = "
//											+ exR.getStop() + " text = "
//											+ exR.getFormText());
						
						exR.setAbsent(rec.getAbsent());
						exR.setAverage(rec.getAverage());
						exR.setReleased(rec.getReleased());
						exR.setSick(rec.getSick());
						exR.setTotal(rec.getTotal());
						
						// make sure we have only fresh marks
						exR.deleteMarks();
						
						@SuppressWarnings("unused")
						long u = exR.update();						
						rec = exR;
						
//						if (BuildConfig.DEBUG)
//							Log.d("GshisHTMLParser", TS.get()
//									+ " GradeRec.update() = " + u);
					}
					else
					{
//						if (BuildConfig.DEBUG)
//							Log.d("GshisHTMLParser", TS.get()
//									+ " insert GradeRec = " + rec);
						
						sch.addGradeRec(rec);
					}

					for (Element td : tr.getElementsByTag("td")) {

						if (td.hasAttr("class")
								&& td.attr("class").equals("grade-with-type")) {


							Element span = td.getElementsByTag("span").first();

							if (containsPrintableChars(span.text())
									&& containsPrintableChars(span
											.attr("title"))) {

								MarkRec mr = rec.getMarkRecByComment(span.attr("title"));
								if (mr != null) {

									mr.setMarks(span.text());
									
									@SuppressWarnings("unused")
									long u = mr.update();

//									if (BuildConfig.DEBUG)
//										Log.d("GshisHTMLParser", TS.get() + " MarkRec.update() = " + u
//												+ " rec = " + rec);
								} else {
									

									mr = new MarkRec(span.text(), span.attr("title"));
									
									mNewMarks.add(mr);
									rec.addMarcRec(mr);

//									if (BuildConfig.DEBUG)
//										Log.d("GshisHTMLParser", TS.get()
//												+ " insert MarkRec Comment = " + mr.getComment() + " Marks = "
//												+ mr.getMarks());
								}
							}
						}
					}
				}
			}
		}
	}
	
	protected static String fixDuplicateString(String newS, String prevS, int idx) {

		if (idx == 0)
			return newS;
		
		if (newS == null || newS.length() == 0)
			return prevS;
		
		if (prevS == null || prevS.length() == 0)
			return newS;
		
		if(idx == 1) {
			return "1) " + prevS + "; 2) " + newS;
		}
		
		return prevS + "; " + Integer.toString(idx) + ") " + newS; 
	}

	public static String getVIEWSTATE(Document doc) {

		Element viewstate = doc.getElementById("__VIEWSTATE");
		if (viewstate == null)
			return null;

		return viewstate.val();
	}
}