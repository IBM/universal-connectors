# frozen_string_literal: true
# = PStore -- Tran***REMOVED***ctional File Storage for Ruby Objects
#
# pstore.rb -
#   originally by matz
#   documentation by Kev Jackson and James Edward Gray II
#   improved by Hongli Lai
#
# See PStore for documentation.

require "digest"

#
# PStore implements a file based persistence mechanism based on a Hash.  User
# code can store hierarchies of Ruby objects (values) into the data store file
# by name (keys).  An object hierarchy may be just a single object.  User code
# may later read values back from the data store or even update data, as needed.
#
# The tran***REMOVED***ctional behavior ensures that any changes succeed or fail together.
# This can be used to ensure that the data store is not left in a transitory
# state, where some values were updated but others were not.
#
# Behind the scenes, Ruby objects are stored to the data store file with
# Marshal.  That carries the usual limitations.  Proc objects cannot be
# marshalled, for example.
#
# == U***REMOVED***ge example:
#
#  require "pstore"
#
#  # a mock wiki object...
#  class WikiPage
#    def initialize( page_name, author, contents )
#      @page_name = page_name
#      @revisions = Array.new
#
#      add_revision(author, contents)
#    end
#
#    attr_reader :page_name
#
#    def add_revision( author, contents )
#      @revisions << { :created  => Time.now,
#                      :author   => author,
#                      :contents => contents }
#    end
#
#    def wiki_page_references
#      [@page_name] + @revisions.last[:contents].scan(/\b(?:[A-Z]+[a-z]+){2,}/)
#    end
#
#    # ...
#  end
#
#  # create a new page...
#  home_page = WikiPage.new( "HomePage", "James Edward Gray II",
#                            "A page about the JoysOfDocumentation..." )
#
#  # then we want to update page data and the index together, or not at all...
#  wiki = PStore.new("wiki_pages.pstore")
#  wiki.tran***REMOVED***ction do  # begin tran***REMOVED***ction; do all of this or none of it
#    # store page...
#    wiki[home_page.page_name] = home_page
#    # ensure that an index has been created...
#    wiki[:wiki_index] ||= Array.new
#    # update wiki index...
#    wiki[:wiki_index].push(*home_page.wiki_page_references)
#  end                   # commit changes to wiki data store file
#
#  ### Some time later... ###
#
#  # read wiki data...
#  wiki.tran***REMOVED***ction(true) do  # begin read-only tran***REMOVED***ction, no changes allowed
#    wiki.roots.each do |data_root_name|
#      p data_root_name
#      p wiki[data_root_name]
#    end
#  end
#
# == Tran***REMOVED***ction modes
#
# By default, file integrity is only ensured as long as the operating system
# (and the underlying hardware) doesn't raise any unexpected I/O errors. If an
# I/O error occurs while PStore is writing to its file, then the file will
# become corrupted.
#
# You can prevent this by setting <em>pstore.ultra_***REMOVED***fe = true</em>.
# However, this results in a minor performance loss, and only works on platforms
# that support atomic file renames. Please consult the documentation for
# +ultra_***REMOVED***fe+ for details.
#
# Needless to ***REMOVED***y, if you're storing valuable data with PStore, then you should
# backup the PStore files from time to time.
class PStore
  RDWR_ACCESS = {mode: IO::RDWR | IO::CREAT | IO::BINARY, encoding: Encoding::ASCII_8BIT}.freeze
  RD_ACCESS = {mode: IO::RDONLY | IO::BINARY, encoding: Encoding::ASCII_8BIT}.freeze
  WR_ACCESS = {mode: IO::WRONLY | IO::CREAT | IO::TRUNC | IO::BINARY, encoding: Encoding::ASCII_8BIT}.freeze

  # The error type thrown by all PStore methods.
  class Error < StandardError
  end

  # Whether PStore should do its best to prevent file corruptions, even when under
  # unlikely-to-occur error conditions such as out-of-space conditions and other
  # unusual OS filesystem errors. Setting this flag comes at the price in the form
  # of a performance loss.
  #
  # This flag only has effect on platforms on which file renames are atomic (e.g.
  # all POSIX platforms: Linux, MacOS X, FreeBSD, etc). The default value is false.
  attr_accessor :ultra_***REMOVED***fe

  #
  # To construct a PStore object, pass in the _file_ path where you would like
  # the data to be stored.
  #
  # PStore objects are always reentrant. But if _thread_***REMOVED***fe_ is set to true,
  # then it will become thread-***REMOVED***fe at the cost of a minor performance hit.
  #
  def initialize(file, thread_***REMOVED***fe = false)
    dir = File::dirname(file)
    unless File::directory? dir
      raise PStore::Error, format("directory %s does not exist", dir)
    end
    if File::exist? file and not File::readable? file
      raise PStore::Error, format("file %s not readable", file)
    end
    @filename = file
    @abort = false
    @ultra_***REMOVED***fe = false
    @thread_***REMOVED***fe = thread_***REMOVED***fe
    @lock = Thread::Mutex.new
  end

  # Raises PStore::Error if the calling code is not in a PStore#tran***REMOVED***ction.
  def in_tran***REMOVED***ction
    raise PStore::Error, "not in tran***REMOVED***ction" unless @lock.locked?
  end
  #
  # Raises PStore::Error if the calling code is not in a PStore#tran***REMOVED***ction or
  # if the code is in a read-only PStore#tran***REMOVED***ction.
  #
  def in_tran***REMOVED***ction_wr
    in_tran***REMOVED***ction
    raise PStore::Error, "in read-only tran***REMOVED***ction" if @rdonly
  end
  private :in_tran***REMOVED***ction, :in_tran***REMOVED***ction_wr

  #
  # Retrieves a value from the PStore file data, by _name_.  The hierarchy of
  # Ruby objects stored under that root _name_ will be returned.
  #
  # *WARNING*:  This method is only valid in a PStore#tran***REMOVED***ction.  It will
  # raise PStore::Error if called at any other time.
  #
  def [](name)
    in_tran***REMOVED***ction
    @table[name]
  end
  #
  # This method is just like PStore#[], ***REMOVED***ve that you may also provide a
  # _default_ value for the object.  In the event the specified _name_ is not
  # found in the data store, your _default_ will be returned instead.  If you do
  # not specify a default, PStore::Error will be raised if the object is not
  # found.
  #
  # *WARNING*:  This method is only valid in a PStore#tran***REMOVED***ction.  It will
  # raise PStore::Error if called at any other time.
  #
  def fetch(name, default=PStore::Error)
    in_tran***REMOVED***ction
    unless @table.key? name
      if default == PStore::Error
        raise PStore::Error, format("undefined root name `%s'", name)
      else
        return default
      end
    end
    @table[name]
  end
  #
  # Stores an individual Ruby object or a hierarchy of Ruby objects in the data
  # store file under the root _name_.  Assigning to a _name_ already in the data
  # store clobbers the old data.
  #
  # == Example:
  #
  #  require "pstore"
  #
  #  store = PStore.new("data_file.pstore")
  #  store.tran***REMOVED***ction do  # begin tran***REMOVED***ction
  #    # load some data into the store...
  #    store[:single_object] = "My data..."
  #    store[:obj_hierarchy] = { "Kev Jackson" => ["rational.rb", "pstore.rb"],
  #                              "James Gray"  => ["erb.rb", "pstore.rb"] }
  #  end                   # commit changes to data store file
  #
  # *WARNING*:  This method is only valid in a PStore#tran***REMOVED***ction and it cannot
  # be read-only.  It will raise PStore::Error if called at any other time.
  #
  def []=(name, value)
    in_tran***REMOVED***ction_wr
    @table[name] = value
  end
  #
  # Removes an object hierarchy from the data store, by _name_.
  #
  # *WARNING*:  This method is only valid in a PStore#tran***REMOVED***ction and it cannot
  # be read-only.  It will raise PStore::Error if called at any other time.
  #
  def delete(name)
    in_tran***REMOVED***ction_wr
    @table.delete name
  end

  #
  # Returns the names of all object hierarchies currently in the store.
  #
  # *WARNING*:  This method is only valid in a PStore#tran***REMOVED***ction.  It will
  # raise PStore::Error if called at any other time.
  #
  def roots
    in_tran***REMOVED***ction
    @table.keys
  end
  #
  # Returns true if the supplied _name_ is currently in the data store.
  #
  # *WARNING*:  This method is only valid in a PStore#tran***REMOVED***ction.  It will
  # raise PStore::Error if called at any other time.
  #
  def root?(name)
    in_tran***REMOVED***ction
    @table.key? name
  end
  # Returns the path to the data store file.
  def path
    @filename
  end

  #
  # Ends the current PStore#tran***REMOVED***ction, committing any changes to the data
  # store immediately.
  #
  # == Example:
  #
  #  require "pstore"
  #
  #  store = PStore.new("data_file.pstore")
  #  store.tran***REMOVED***ction do  # begin tran***REMOVED***ction
  #    # load some data into the store...
  #    store[:one] = 1
  #    store[:two] = 2
  #
  #    store.commit        # end tran***REMOVED***ction here, committing changes
  #
  #    store[:three] = 3   # this change is never reached
  #  end
  #
  # *WARNING*:  This method is only valid in a PStore#tran***REMOVED***ction.  It will
  # raise PStore::Error if called at any other time.
  #
  def commit
    in_tran***REMOVED***ction
    @abort = false
    throw :pstore_abort_tran***REMOVED***ction
  end
  #
  # Ends the current PStore#tran***REMOVED***ction, discarding any changes to the data
  # store.
  #
  # == Example:
  #
  #  require "pstore"
  #
  #  store = PStore.new("data_file.pstore")
  #  store.tran***REMOVED***ction do  # begin tran***REMOVED***ction
  #    store[:one] = 1     # this change is not applied, see below...
  #    store[:two] = 2     # this change is not applied, see below...
  #
  #    store.abort         # end tran***REMOVED***ction here, discard all changes
  #
  #    store[:three] = 3   # this change is never reached
  #  end
  #
  # *WARNING*:  This method is only valid in a PStore#tran***REMOVED***ction.  It will
  # raise PStore::Error if called at any other time.
  #
  def abort
    in_tran***REMOVED***ction
    @abort = true
    throw :pstore_abort_tran***REMOVED***ction
  end

  #
  # Opens a new tran***REMOVED***ction for the data store.  Code executed inside a block
  # passed to this method may read and write data to and from the data store
  # file.
  #
  # At the end of the block, changes are committed to the data store
  # automatically.  You may exit the tran***REMOVED***ction early with a call to either
  # PStore#commit or PStore#abort.  See those methods for details about how
  # changes are handled.  Raising an uncaught Exception in the block is
  # equivalent to calling PStore#abort.
  #
  # If _read_only_ is set to +true+, you will only be allowed to read from the
  # data store during the tran***REMOVED***ction and any attempts to change the data will
  # raise a PStore::Error.
  #
  # Note that PStore does not support nested tran***REMOVED***ctions.
  #
  def tran***REMOVED***ction(read_only = false)  # :yields:  pstore
    value = nil
    if !@thread_***REMOVED***fe
      raise PStore::Error, "nested tran***REMOVED***ction" unless @lock.try_lock
    else
      begin
        @lock.lock
      rescue ThreadError
        raise PStore::Error, "nested tran***REMOVED***ction"
      end
    end
    begin
      @rdonly = read_only
      @abort = false
      file = open_and_lock_file(@filename, read_only)
      if file
        begin
          @table, checksum, original_data_size = load_data(file, read_only)

          catch(:pstore_abort_tran***REMOVED***ction) do
            value = yield(self)
          end

          if !@abort && !read_only
            ***REMOVED***ve_data(checksum, original_data_size, file)
          end
        ensure
          file.close
        end
      else
        # This can only occur if read_only == true.
        @table = {}
        catch(:pstore_abort_tran***REMOVED***ction) do
          value = yield(self)
        end
      end
    ensure
      @lock.unlock
    end
    value
  end

  private
  # Constant for relieving Ruby's garbage collector.
  CHECKSUM_ALGO = %w[SHA512 SHA384 SHA256 SHA1 RMD160 MD5].each do |algo|
    begin
      break Digest(algo)
    rescue LoadError
    end
  end
  EMPTY_STRING = ""
  EMPTY_MARSHAL_DATA = Marshal.dump({})
  EMPTY_MARSHAL_CHECKSUM = CHECKSUM_ALGO.digest(EMPTY_MARSHAL_DATA)

  #
  # Open the specified filename (either in read-only mode or in
  # read-write mode) and lock it for reading or writing.
  #
  # The opened File object will be returned. If _read_only_ is true,
  # and the file does not exist, then nil will be returned.
  #
  # All exceptions are propagated.
  #
  def open_and_lock_file(filename, read_only)
    if read_only
      begin
        file = File.new(filename, RD_ACCESS)
        begin
          file.flock(File::LOCK_SH)
          return file
        rescue
          file.close
          raise
        end
      rescue Errno::ENOENT
        return nil
      end
    else
      file = File.new(filename, RDWR_ACCESS)
      file.flock(File::LOCK_EX)
      return file
    end
  end

  # Load the given PStore file.
  # If +read_only+ is true, the unmarshalled Hash will be returned.
  # If +read_only+ is false, a 3-tuple will be returned: the unmarshalled
  # Hash, a checksum of the data, and the size of the data.
  def load_data(file, read_only)
    if read_only
      begin
        table = load(file)
        raise Error, "PStore file seems to be corrupted." unless table.is_a?(Hash)
      rescue EOFError
        # This seems to be a newly-created file.
        table = {}
      end
      table
    else
      data = file.read
      if data.empty?
        # This seems to be a newly-created file.
        table = {}
        checksum = empty_marshal_checksum
        size = empty_marshal_data.bytesize
      else
        table = load(data)
        checksum = CHECKSUM_ALGO.digest(data)
        size = data.bytesize
        raise Error, "PStore file seems to be corrupted." unless table.is_a?(Hash)
      end
      data.replace(EMPTY_STRING)
      [table, checksum, size]
    end
  end

  def on_windows?
    is_windows = RUBY_PLATFORM =~ /mswin|mingw|bccwin|wince/
    self.class.__send__(:define_method, :on_windows?) do
      is_windows
    end
    is_windows
  end

  def ***REMOVED***ve_data(original_checksum, original_file_size, file)
    new_data = dump(@table)

    if new_data.bytesize != original_file_size || CHECKSUM_ALGO.digest(new_data) != original_checksum
      if @ultra_***REMOVED***fe && !on_windows?
        # Windows doesn't support atomic file renames.
        ***REMOVED***ve_data_with_atomic_file_rename_strategy(new_data, file)
      else
        ***REMOVED***ve_data_with_fast_strategy(new_data, file)
      end
    end

    new_data.replace(EMPTY_STRING)
  end

  def ***REMOVED***ve_data_with_atomic_file_rename_strategy(data, file)
    temp_filename = "#{@filename}.tmp.#{Process.pid}.#{rand 1000000}"
    temp_file = File.new(temp_filename, WR_ACCESS)
    begin
      temp_file.flock(File::LOCK_EX)
      temp_file.write(data)
      temp_file.flush
      File.rename(temp_filename, @filename)
    rescue
      File.unlink(temp_file) rescue nil
      raise
    ensure
      temp_file.close
    end
  end

  def ***REMOVED***ve_data_with_fast_strategy(data, file)
    file.rewind
    file.write(data)
    file.truncate(data.bytesize)
  end


  # This method is just a wrapped around Marshal.dump
  # to allow subclass overriding used in YAML::Store.
  def dump(table)  # :nodoc:
    Marshal::dump(table)
  end

  # This method is just a wrapped around Marshal.load.
  # to allow subclass overriding used in YAML::Store.
  def load(content)  # :nodoc:
    Marshal::load(content)
  end

  def empty_marshal_data
    EMPTY_MARSHAL_DATA
  end
  def empty_marshal_checksum
    EMPTY_MARSHAL_CHECKSUM
  end
end
