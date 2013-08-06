package com.keba.teachdroid.app;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.keba.teachdroid.app.fragments.ProgramCodeFragment;
import com.keba.teachdroid.app.fragments.ProgramInfoFragment;
import com.keba.teachdroid.app.fragments.ProjectDetailFragment;
import com.keba.teachdroid.app.fragments.ProjectListFragment;
import com.keba.teachdroid.app.fragments.ProjectNestingFragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

public class ProjectActivity extends FragmentActivity implements Serializable {

	ViewPager mViewPager;
	SectionsPagerAdapter mSectionsPagerAdapter;

	// dummy contents for the Pages!
	String[] projects = { "Project1", "Project2", "Project3" };
	Vector<String[]> programs = new Vector<String[]>();
	Map<Integer, String> programCodes = new HashMap<Integer, String>();
	Map<Integer, String> programInfos = new HashMap<Integer, String>();

	int selectedProject = 0;
	int selectedProgram = 0;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_project_swipe);
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// dummy content building
		String[] programs1 = { "Prog1", "Prog2", "Prog3" };
		String[] programs2 = { "Prog44", "Prog45", "Prog46" };
		String[] programs3 = { "Prog7", "Prog8", "Prog9" };
		programs.add(programs1);
		programs.add(programs2);
		programs.add(programs3);
	}

	public String[] getProjects() {
		return projects;
	}

	public String[] getPrograms() {
		return programs.get(selectedProject);
	}

	public String getProgramCode() {
		if (programCodes != null) {
			return programCodes.get(Integer.valueOf(selectedProgram));
		} else {
			return "No Program selected";
		}
	}

	public String getProgramInfo() {
		return null;
	}

	public int getSelectedProject() {
		return selectedProject;
	}

	public void setSelectedProject(int selectedProject) {
		this.selectedProject = selectedProject;
		((ProjectNestingFragment) mSectionsPagerAdapter.getItem(0)).setProgramListContent();
		mViewPager.invalidate();
	}

	public int getSelectedProgram() {
		return selectedProgram;
	}

	public void setSelectedProgram(int selectedProgram) {
		this.selectedProgram = selectedProgram;
		programCodes.clear();
		programCodes.put(Integer.valueOf(0), "Project:" + projects[selectedProject] + "\nProgram:" + getPrograms()[selectedProgram] + "\nLIN(cp0)\nPTP(ap0)");
		programCodes.put(Integer.valueOf(1), "Project:" + projects[selectedProject] + "\nProgram:" + getPrograms()[selectedProgram] + "\nLIN(cp0)\nPTP(ap0)");
		programCodes.put(Integer.valueOf(2), "Project:" + projects[selectedProject] + "\nProgram:" + getPrograms()[selectedProgram] + "\nLIN(cp0)\nPTP(ap0)");
		((ProgramCodeFragment) mSectionsPagerAdapter.getItem(1)).setProgramCode();

		programInfos.clear();
		programInfos.put(Integer.valueOf(0), "Project:" + projects[selectedProject] + "\nProgram:" + getPrograms()[selectedProgram] + "\nrunning..");
		programInfos.put(Integer.valueOf(1), "Project:" + projects[selectedProject] + "\nProgram:" + getPrograms()[selectedProgram] + "\nrunning..");
		programInfos.put(Integer.valueOf(2), "Project:" + projects[selectedProject] + "\nProgram:" + getPrograms()[selectedProgram] + "\nrunning..");
		((ProgramInfoFragment) mSectionsPagerAdapter.getItem(2)).setProgramInfo();

		mViewPager.setCurrentItem(1);

	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		private Fragment[] mFragments;

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);

		}

		@Override
		public Fragment getItem(int position) {

			if (position > getCount() - 1)
				return null;

			if (mFragments == null) {
				Bundle args = new Bundle();
				mFragments = new Fragment[] { new ProjectNestingFragment(), new ProgramCodeFragment(), new ProgramInfoFragment() }; /*
																																	 * new
																																	 * ProjectListFragment
																																	 * (
																																	 * )
																																	 * ,
																																	 * new
																																	 * ProjectDetailFragment
																																	 * (
																																	 * )
																																	 */

				args.putSerializable("connector", ProjectActivity.this);
				mFragments[0].setArguments(args);
				mFragments[1].setArguments(args);
				mFragments[2].setArguments(args);
			}

			return mFragments[position];
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0:
				return getString(R.string.title_project_list);
			case 1:
				return getString(R.string.title_section_program_code);
				// case 2:
				// return getString(R.string.title_project_detail);
			default:
				return "NOT_DEFINED_" + position;
			}
		}
	}

}