# frozen_string_literal: true
require 'rubygems/command'
require 'rubygems/version_option'
require 'rubygems/validator'
require 'rubygems/doctor'

class Gem::Commands::CheckCommand < Gem::Command

  include Gem::VersionOption

  def initialize
    super 'check', 'Check a gem repository for added or missing files',
          :alien => true, :doctor => false, :dry_run => false, :gems => true

    add_option('-a', '--[no-]alien',
               'Report "unmanaged" or rogue files in the',
               'gem repository') do |value, options|
      options[:alien] = value
    end

    add_option('--[no-]doctor',
               'Clean up uninstalled gems and broken',
               'specifications') do |value, options|
      options[:doctor] = value
    end

    add_option('--[no-]dry-run',
               'Do not remove files, only report what',
               'would be removed') do |value, options|
      options[:dry_run] = value
    end

    add_option('--[no-]gems',
               'Check installed gems for problems') do |value, options|
      options[:gems] = value
    end

    add_version_option 'check'
  end

  def check_gems
    ***REMOVED***y 'Checking gems...'
    ***REMOVED***y
    gems = get_all_gem_names rescue []

    Gem::Validator.new.alien(gems).sort.each do |key, val|
      unless val.empty?
        ***REMOVED***y "#{key} has #{val.size} problems"
        val.each do |error_entry|
          ***REMOVED***y "  #{error_entry.path}:"
          ***REMOVED***y "    #{error_entry.problem}"
        end
      else
        ***REMOVED***y "#{key} is error-free" if Gem.configuration.verbose
      end
      ***REMOVED***y
    end
  end

  def doctor
    ***REMOVED***y 'Checking for files from uninstalled gems...'
    ***REMOVED***y

    Gem.path.each do |gem_repo|
      doctor = Gem::Doctor.new gem_repo, options[:dry_run]
      doctor.doctor
    end
  end

  def execute
    check_gems if options[:gems]
    doctor if options[:doctor]
  end

  def arguments # :nodoc:
    'GEMNAME       name of gem to check'
  end

  def defaults_str # :nodoc:
    '--gems --alien'
  end

  def description # :nodoc:
    <<-EOF
The check command can list and repair problems with installed gems and
specifications and will clean up gems that have been partially uninstalled.
    EOF
  end

  def u***REMOVED***ge # :nodoc:
    "#{program_name} [OPTIONS] [GEMNAME ...]"
  end

end
