using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Data.Entity;
using System.Linq;
using System.Web;

namespace VISIDA_API.Models
{
    public class PagingModel
    {
        public int PageNumber { get; set; } = 1;

        private int _pageSize { get; set; } = -1;
        public int PageSize
        {

            get { return _pageSize; }
            set { _pageSize = value; }
        }

        private int _totalCount;
        public int TotalCount
        {
            get { return _totalCount; }
            set
            {
                if (_pageSize < 0)
                    _pageSize = value;
                _totalCount = value;
            }
        }

        public int TotalPages
        {
            get
            {
                return (int)Math.Ceiling(TotalCount / (double)PageSize);
            }
        }

        public IQueryable<T> GetPage<T>(DbSet<T> set) where T : class
        {
            return set.Skip((PageNumber - 1) * PageSize).Take(PageSize);
        }

        public IQueryable<T> GetPage<T>(IQueryable<T> queryable) where T : class
        {
            return queryable.Skip((PageNumber - 1) * PageSize).Take(PageSize);
        }

        public ICollection<T> GetPage<T>(ICollection<T> queryable) where T : class
        {
            return queryable.Skip((PageNumber - 1) * PageSize).Take(PageSize).ToList();
        }

        public IEnumerable<T> GetPage<T>(IEnumerable<T> queryable) where T : class
        {
            return queryable.Skip((PageNumber - 1) * PageSize).Take(PageSize);
        }

        public object GetMetadata()
        {
            bool hasPrev = PageNumber > 1;
            bool hasNext = PageNumber < TotalPages;
            return new
            {
                totalCount = TotalCount,
                pageSize = PageSize,
                currentPage = PageNumber,
                totalPages = TotalPages,
                hasPrev,
                hasNext
            };
        }
    }
}